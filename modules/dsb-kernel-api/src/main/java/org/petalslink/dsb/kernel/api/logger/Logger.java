/**
 * 
 */
package org.petalslink.dsb.kernel.api.logger;

/**
 * @author chamerling
 * 
 */
public class Logger {

    private String name;

    private String level;

    public Logger() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

}
