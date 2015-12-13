/**
 *  DSC Alarm Button
 *
 *  Author: David Cauthron
 *  Original Author: Carlos Santiago <carloss66@gmail.com>
 *  Original Code By: Rob Fisher <robfish@att.net>
 *  Date: 12/12/2015
 */
 // for the UI
 
metadata {
	// Automatically generated. Make future change here.
	definition (name: "DSC Single Button", author: "David Cauthron") {
		capability "Switch"
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: 'Arm', action: "switch.on", icon: "st.Home.home2", backgroundColor: "#79b821"
			state "on", label: 'Disarm', action: "switch.off", icon: "st.Home.home2", backgroundColor: "#800000"
    	}

		main "button"
        details(["button"])
	}
}

def on() {
    sendEvent (name: "switch", value: "on")
}

def off() {
    sendEvent (name: "switch", value: "off")
}