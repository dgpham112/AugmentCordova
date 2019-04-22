// This is an example of your own business logic
function addProductToCart(product) {
    // Do something here
    console.log("add product to cart", product);
}

var productToSearch = {
    identifier: "1",
    brand: "Rowenta",
    name: "AIR Force Extreme",
    ean: "3700342425321"
};

// Demo credentials, please replace with yours
var credentials = {
    id:  "357fee36746668573ceb2f5957c4869ee1a62a112639bac9b0fae43c7c431692",
    key: "80ae1420e164e0440d5329067bcdd953e9fa6c63b75c001c06d169a4f11268c5",
    vuforia: "ATQqCM7/////AAAAGXLs+GRi0UwXh0X+/qQL49dbZGym8kKo+iRtgC95tbJoCWjXXZihDl5pzxoca2JxLcYxBJ2pIeIE4dNcK0etMeb1746L7lq6vSFen43cS7P1P/HXjwHtUouV5Xus2U0F7WHUTKuO629jKFO13fBQczuY52UJcSEhsu9jHPMaupo5CpqQT3TFTQjlhzHhVXiVMEqq7RI+Edwh8TCSfGAbNRdbIELTfK+8YDYqwEHDbp62mFrs68YnCEQZDrpcLyC8WzFCVZtnUq3Cj3YBUfQ6gNnENYiuLf06gAAF/FcaF65VYveGRBbp3hpkqolX28bxPiUYNVknCSFXICPHciVntxF+rcHW5rrX7Cg/IVFGdNRF"
}

// UIElement javascript code is executed in a valid Augment Session
var uiElements = [
    {
        title: "Share",
        code: function (player) {
            player.shareScreenshot();
        }
    },
    {
        title: "Alert",
        code: function (player) {
            player.showAlertMessage("My alert", "My message", "yay!");
        }
    },
    {
        title: "Buy",
        code: function (player) {
            addProductToCart(productToSearch);
            // Then close the ARView
            player.stop(function (success) {
                // Not much to do :)
            });
        }
    },
    {
        title: "Center",
        code: function (player) {
            player.recenterProducts();
        },
        color: "#FF0000",           // Title color #XXXXXX format string
        borderSize: "3",            // Border size in point, has to be a string
        borderRadius: "5",          // Border radius in point, has to be a string
        borderColor: "#00FFFF",     // Border color #XXXXXX format string
        fontSize: "22",             // Font size in point, has to be a string
        backgroundColor: "#0000FF"  // Background color #XXXXXX format string
    }
];

/**
 * Example one, query the API first and if the product exists start the ARView
 * You keep the control over errors (API error or product not found) before the ARView starts.
 * This is the recommended way
 */
function exampleAugmentSession1() {
    if (!AugmentCordova) {
        console.log('AugmentCordova not loaded');
        alert('AugmentCordova not loaded');
    }

    AugmentCordova.init({
        id:  credentials.id,
        key: credentials.key,
        vuforia: credentials.vuforia,
        uiElements: uiElements
    });

    AugmentCordova.checkIfModelDoesExistForUserProduct(productToSearch, function (productFound) {
        // Check if the Augment API found a corresponding Product
        if (!productFound) {
            alert('Product not found');
            console.log("No Product found");
            return;
        }

        // Start the ARView
        AugmentCordova.startPlayer(function (player) {
            // Add this Product to the Augmented View and start
            player.addProduct(productFound);
        }, function (error) {
            alert('The Augment View could not be loaded. ' + error.error);
        });
    }, function (error) {
        alert('Augment API Error: ' + error.error);
    });
}


/**
 * Example two, start the ARView with the product (it will be queried automaticaly)
 * Errors will be handled in the ARView, the ARView will start even if the product is not found.
 */
function exampleAugmentSession2() {
    if (!AugmentCordova) {
        console.log('AugmentCordova not loaded');
        alert('AugmentCordova not loaded');
    }

    AugmentCordova.init({
        id:  credentials.id,
        key: credentials.key,
        vuforia: credentials.vuforia,
        uiElements: uiElements
    });

    // Start the ARView
    AugmentCordova.startPlayer(function (player) {
        // Start and query for this Product
        player.addProduct(productToSearch);
    }, function (error) {
        alert('The Augment View could not be loaded. ' + error.error);
    });
}

var app = {
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function() {
        this.receivedEvent('deviceready');
        exampleAugmentSession1();
    },

    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');
        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};

app.initialize();
