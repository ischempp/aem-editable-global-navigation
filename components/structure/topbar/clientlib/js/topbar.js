(function($) {

    $flexhead = $("#flex-head");
    $(".search-container").on("click", function(e){
        if (!$flexhead.hasClass("search-active")) $flexhead.addClass("search-active");
        e.stopPropagation();
    });
    $(document).on("click", function(e) {
        if ($flexhead.hasClass("search-active")) $flexhead.removeClass("search-active");
    });
    $("#search").on("click", function(){
        this.value = "";
    });
    $window = $(window);
    $window.scroll(function() {
        var $scroll_position = $window.scrollTop(),
            $header = $("header"),
            $offCanvasNav = $("#offCanvas");
        if ($scroll_position > 0) {
            $header.addClass("sticky").find("a.sprites").removeClass("fh-logo-tagline").addClass("fh-logo");
            $offCanvasNav.addClass("scrolled");
        } else {
            $header.removeClass("sticky").find("a.sprites").removeClass("fh-logo").addClass("fh-logo-tagline");
            $offCanvasNav.removeClass("scrolled");
        }
    });
  
})(jQuery);