# Develop

## Common

To develop new features for this plugin, you need an app using Cordova  

Install cordova by following these steps:   [https://cordova.apache.org/docs/en/latest/guide/cli/index.html](https://cordova.apache.org/docs/en/latest/guide/cli/index.html)  

Then it is quite easy

*On this setup I put the cordova app in the `./apps` directory, paths in the following commands will follow that convention*

Create a new Cordova app:

```
$ mkdir apps; cd apps
$ cordova create cordova com.acme.myapp myapp
$ cd cordova
$ cordova platform add android ios --save # you can choose to install only one
$ cd ../..
```

Install our plugin:
```
$ git clone git@github.com:Augment/AugmentCordova.git
$ cd apps/cordova
$ cordova plugin add ../../AugmentCordova
```

## Native code

After installing the plugin, you will have a valid Android/iOS app in `apps/cordova/platforms/*`  
You can open it with you favorite IDE to review and test it, and edit the native bridge.

If needed you can find an example of a working Javascript to get started faster  
checkout `AugmentCordova/doc/cordova-example-index.js` and copy past it in `apps/cordova/www/js/index.js` to test your modifications.

Uninstall our plugin:
```
$ cd apps/cordova
$ cordova plugin remove "AugmentCordova"
```

## Javascript code

Install npm dependencies:
```
$ cd AugmentCordova
$ npm install
```

Then you must `npm run watch` for Javascript modifications as we use flowtype to type check the code.  
We encourage you to use Atom editor with the Nuclide plugin so you will benefit from type-flow.

[https://flowtype.org/docs/getting-started.html](https://flowtype.org/docs/getting-started.html)

Then you can edit the Javascript bridge in `src/AugmentCordova.js`
