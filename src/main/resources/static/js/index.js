$(document).ready(function () {
    // Set default headers for AJAX requests
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json'
        }
    });

    // Function to handle submission of license information
    window.submitLicenseInfo = function () {
        let licenseInfo = {
            licenseeName: $('#licenseeName').val(),
            assigneeName: $('#assigneeName').val(),
            expiryDate: $('#expiryDate').val()
        };
        localStorage.setItem('licenseInfo', JSON.stringify(licenseInfo));
        $('#mask, #form').hide();
    };

    // Function to handle search input
    $('#search').on('input', function (e) {
        $("#product-list").load('/search?search=' + e.target.value);
    });

    // Function to show license form
    window.showLicenseForm = function () {
        let licenseInfo = JSON.parse(localStorage.getItem('licenseInfo'));
        $('#licenseeName').val(licenseInfo?.licenseeName || 'Azuelane');
        $('#assigneeName').val(licenseInfo?.assigneeName || 'Yamato');
        $('#expiryDate').val(licenseInfo?.expiryDate || '2030-12-31');
        $('#mask, #form').show();
    };

    // Function to show VM options
    window.showVmoptins = function () {
        $('#config').val("--add-opens=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED\n" +
            "--add-opens=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED\n" +
            "-javaagent:/(Your Path)/ja-netfilter/ja-netfilter.jar")
        $('#mask, #vmoptions').show();
    };

    // Function to copy license
    window.copyLicense = async function (e) {
        while (localStorage.getItem('licenseInfo') === null) {
            $('#mask, #form').show();
            await new Promise(r => setTimeout(r, 1000));
        }
        let licenseInfo = JSON.parse(localStorage.getItem('licenseInfo'));
        let productCode = $(e).closest('.card').data('productCodes');
        let data = {
            "licenseName": licenseInfo.licenseeName,
            "assigneeName": licenseInfo.assigneeName,
            "expiryDate": licenseInfo.expiryDate,
            "productCode": productCode,
        };
        if ("FinalShell" === productCode) {
            const machineCode = prompt("请输入机器码：", localStorage.getItem('machineCode'))
            if (machineCode != null && machineCode !== "") {
                data["machineCode"] = machineCode
                localStorage.setItem('machineCode', machineCode)
            } else {
                alert("未输入机器码，已取消后续操作")
                return
            }
        }
        $.post('/generateLicense', JSON.stringify(data))
            .then(response => {
                if (401 === response.code) {
                    window.location.href = "/"
                    return
                }
                copyText(response)
                    .then(() => {
                        e.setAttribute('data-content', 'Copied!');
                    })
                    .catch(() => {
                        e.setAttribute('data-content', 'Copy failed!');
                    })
                    .finally(() => {
                        setTimeout(() => {
                            e.setAttribute('data-content', 'Copy to clipboard');
                        }, 2000);
                    });
            })
            .catch(() => {
                e.setAttribute('data-content', 'Copy failed!');
                setTimeout(() => {
                    e.setAttribute('data-content', 'Copy to clipboard');
                }, 2000);
            });
    };

// Function to copy text to clipboard
    const copyText = async (val) => {
        if (navigator.clipboard && navigator.permissions) {
            await navigator.clipboard.writeText(val);
            return "The activation code has been copied";
        } else {
            alert("复制失败，请手动复制：" + val)
            return "The system does not support it, please go to the console to copy it manually";
        }
    };

});
