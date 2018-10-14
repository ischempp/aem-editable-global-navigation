(function($) {

    var EMAIL_SELECTOR = "fh.globalnav",

    foundationReg = $(window).adaptTo("foundation-registry");

    foundationReg.register("foundation.validation.validator", {
        selector: "[data-validation='" + EMAIL_SELECTOR + "']",
        validate: function(el) {

            /* Looking for a string that ends with globalnavigationsect 
             * but can be appended with an underscore and some numbers */
            var globalNav_pattern = /globalnavigationsect(_\d+)?$/;
            var error_message = "Please select a global navigation section component.";
            var result = el.value.match(globalNav_pattern);

            if (result === null) {
                return error_message;
            }
        }
    });

}(jQuery));
