package io.atlasmap.standalone;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class RoutingConfiguration extends WebMvcConfigurerAdapter {

    public void addViewControllers(final ViewControllerRegistry registry){
        super.addViewControllers(registry);
        registry.addViewController("/mapping-id/*").setViewName("forward:/");
    }
}
