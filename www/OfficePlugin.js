var exec = require('cordova/exec');

exports.openFileByFileUrl = function(success, error,fileurl,isNeedSelect) {
    exec(success, error, "OfficePlugin", "openFileByFileUrl", [fileurl,isNeedSelect]);
};
exports.openFileByFilePath = function(success, error,filePath) {
    exec(success, error, "OfficePlugin", "openFileByFilePath", [filePath]);
};