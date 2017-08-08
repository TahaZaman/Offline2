package dagger.com.fbgraphapi.utils;

import java.io.Serializable;

/**
 * Created by Taha on 8/5/2017.
 */

public class Contact implements Serializable{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String name = null;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    private String deviceName = null;
    private String deviceAddress= null;
    private String status= null;
    private String ipAddress= null;

    private int port= -1;

    @Override
    public boolean equals(Object object) {
        if (object instanceof Contact) {
            Contact c = (Contact) object;

            return (c.getDeviceAddress().equals(this.deviceAddress)) && (c.getName().equals(this.name));
        }
        return false;
    }

    @Override
    public String toString() {
        return "Contact [name=" + name + ", deviceAddress=" + deviceAddress + ", status=" + status
                + ", ipAddress=" + ipAddress + ", port=" + port
                + "]";
    }


    @Override
    public int hashCode() {
        return (this.name + this.deviceAddress).hashCode();
    }

    public static Contact fromString(String s){
        Contact contact = new Contact();

        s = s.substring(s.indexOf("["),s.length() - 1);
        String params[] = s.split(",");
        contact.setName(params[0].split("=")[1]);
        contact.setDeviceAddress(params[1].split("=")[1]);
        contact.setStatus(params[2].split("=")[1]);
        contact.setIpAddress(params[3].split("=")[1]);
        contact.setPort(Integer.parseInt(params[4].split("=")[1]));

        return contact;
    }
}
