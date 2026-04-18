if (!window.__chatAppAnalyticsLoaded) {
    window.__chatAppAnalyticsLoaded = true;

    window.dataLayer = window.dataLayer || [];
    window.gtag = window.gtag || function () {
        window.dataLayer.push(arguments);
    };

    var analyticsScript = document.createElement("script");
    analyticsScript.async = true;
    analyticsScript.src = "https://www.googletagmanager.com/gtag/js?id=G-Q22P661RV4";
    document.head.appendChild(analyticsScript);

    window.gtag("js", new Date());
    window.gtag("config", "G-Q22P661RV4");
}
