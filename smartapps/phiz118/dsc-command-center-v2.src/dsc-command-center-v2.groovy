/**
 *  DSC Command Center
 *
 *  Copyright 2015 David Cauthron
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "DSC Command Center v2",
    namespace: "phiz118",
    author: "David Cauthron",
    description: "Command Center SmartApp for DSC Alarms",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	preferences {
    	section("Alarm Server Settings") {
            input("ip", "text", title: "IP", description: "The IP of your AlarmServer")
            input("port", "text", title: "Port", description: "The port")
        }
        section("Button for Alarm") {
            input "thecommand", "capability.Switch", required: true
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "Version 1.0"
    subscribe(location, "alarmSystemStatus", switchUpdate)
	subscribe(thecommand, "switch", switchUpdate)
}

def switchUpdate(evt) {
	callAlarmServer(evt)
}

private callAlarmServer(evt) {
	try {
        def eventMap = [
          'stayarm':"/api/alarm/stayarm",
          'disarm':"/api/alarm/disarm",
          'arm':"/api/alarm/arm",
          'stay':"/api/alarm/stayarm",
          'off':"/api/alarm/disarm",
          'away':"/api/alarm/arm"
        ]
    
        def path = eventMap."${evt.value}"
        
        sendHubCommand(new physicalgraph.device.HubAction(
            method: "POST",
            path: path,
            headers: [
                HOST: "${ip}:${port}"
            ]
        ))
    } catch (e) {
        log.error "something went wrong: $e"
    }
}