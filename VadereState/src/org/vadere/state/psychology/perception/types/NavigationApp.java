package org.vadere.state.psychology.perception.types;

import com.fasterxml.jackson.databind.JsonNode;

public class NavigationApp extends Stimulus {

    private String info;


    public NavigationApp() {
        super();
    }



    @Override
    public NavigationApp clone() {
        return null;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(final String info) {
        this.info = info;
    }
}
