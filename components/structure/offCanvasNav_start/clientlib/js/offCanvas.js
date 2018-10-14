(function($) {

    var $clickTargets = $(".accordion-menu .is-accordion-submenu-parent > a"),
        TOGGLE_CLASS = "close";

    $clickTargets.click(function(){
        var $this = $(this);
        $this.parent().find("> .sprites.expander").toggleClass(TOGGLE_CLASS);
    });

})(jQuery);