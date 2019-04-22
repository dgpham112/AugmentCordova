// Cordova quick flowtype definition for `exec` function
declare type CordovaExec = (success: any, error: any, pluginName: string, methodName: string, args: any[]) => void;

declare module "cordova/exec" {
    declare module.exports: CordovaExec;
};
