package com.meditreat.app.config;

// All i18n related imports are removed as they are managed in I18nConfig
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // All bean definitions (localeResolver, localeChangeInterceptor, messageSource)
    // and the addInterceptors method related to i18n have been removed.
    // WebMvcConfig will now only contain other MVC related configurations if any.

}