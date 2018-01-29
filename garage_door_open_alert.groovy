/**
 *  Send push alert if overhead garage door is left open for too long.
 *
 *  Author: Rusty Kroboth
 */
definition(
    name: "Garage Door Open Reminder",
    namespace: "rkroboth",
    author: "Rusty Kroboth",
    description: "Get a SmartThings app push notification if the garage door is left open too long",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("Send a push alert when this door is open...") {
		input "contactsensor", "capability.contactSensor", title: "Which?"
	}
	section("For this long...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
    section("Mute alerts?"){
        input "mute_alerts", "bool", title: "Mute alerts?"
    }
}

def installed()
{
	subscribe(contactsensor, "contact", onChange)
}

def updated()
{
	unsubscribe()
	subscribe(contactsensor, "contact", onChange)
}

def onChange(evt) {
	if (evt.value == "closed") {
        state.door_status = null
    }
    if (evt.value == "open") {
        runIn(maxOpenTime * 60, scheduledAction)
        state.door_status = "open"
    }
}

def scheduledAction(){
	if (state.door_status == "open")
	{
        if (mute_alerts == true){
            log.trace "Alerts are muted... Not sending text."
        }
        else {
            log.trace "{$contactsensor} is still open... sending push alert."
            sendPush "Your ${contactsensor.label ?: contactsensor.name} has been open for more than ${maxOpenTime} minutes!"
            runIn(60, scheduledAction)
        }
	}
    else {
		log.trace "{$contactsensor} is not open... Not sending text."
	}
}


