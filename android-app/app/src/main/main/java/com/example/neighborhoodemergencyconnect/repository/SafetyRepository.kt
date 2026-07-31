package com.example.yourpackage.repository

import com.example.neighborhoodemergencyconnect.models.SafetyTip


object SafetyRepository {

    fun getSafetyTips(): List<SafetyTip> {

        return listOf(

            // FIRE
            SafetyTip(
                title = "Fire Safety",
                category = "Fire",
                shortDescription = "Learn how to respond during fire emergencies.",
                description = "Fire emergencies can spread rapidly. Stay calm, evacuate safely, and immediately inform the fire department.",
                dos = listOf(
                    "Stay calm and alert everyone nearby.",
                    "Evacuate the building immediately.",
                    "Use stairs instead of elevators.",
                    "Call the Fire Department (101).",
                    "Stay low if smoke is present."
                ),
                donts = listOf(
                    "Do not panic.",
                    "Do not use elevators.",
                    "Do not re-enter the building.",
                    "Do not hide inside rooms."
                ),
                emergencyNumber = "101"
            ),

            // MEDICAL
            SafetyTip(
                title = "Medical Emergency",
                category = "Medical",
                shortDescription = "First aid and emergency response guidance.",
                description = "Quick medical response can save lives. Call emergency services and provide first aid only if you are trained.",
                dos = listOf(
                    "Call Ambulance (108).",
                    "Keep the patient calm.",
                    "Perform CPR only if trained.",
                    "Monitor breathing and pulse.",
                    "Provide accurate location details."
                ),
                donts = listOf(
                    "Do not move seriously injured patients unnecessarily.",
                    "Do not give food or water to unconscious people.",
                    "Do not crowd around the patient."
                ),
                emergencyNumber = "108"
            ),

            // ACCIDENT
            SafetyTip(
                title = "Road Accident",
                category = "Accident",
                shortDescription = "Steps to follow after a road accident.",
                description = "Ensure your safety first, call emergency services, and assist victims only when it is safe.",
                dos = listOf(
                    "Move to a safe location if possible.",
                    "Call Ambulance (108).",
                    "Call Police if required.",
                    "Switch on hazard lights.",
                    "Help injured people carefully."
                ),
                donts = listOf(
                    "Do not flee the accident scene.",
                    "Do not move victims with spinal injuries.",
                    "Do not block emergency responders."
                ),
                emergencyNumber = "108"
            ),

            // CRIME
            SafetyTip(
                title = "Crime Prevention",
                category = "Crime",
                shortDescription = "Stay alert and report suspicious activities.",
                description = "Personal safety should always come first. Report crimes immediately and avoid direct confrontation.",
                dos = listOf(
                    "Call Police (100).",
                    "Stay in a safe place.",
                    "Observe important details if safe.",
                    "Report suspicious activities immediately."
                ),
                donts = listOf(
                    "Do not confront armed criminals.",
                    "Do not chase suspects.",
                    "Do not share unverified information."
                ),
                emergencyNumber = "100"
            ),

            // FLOOD
            SafetyTip(
                title = "Flood Safety",
                category = "Flood",
                shortDescription = "Safety measures during flood emergencies.",
                description = "Floodwaters can be dangerous. Move to higher ground and avoid entering flooded areas.",
                dos = listOf(
                    "Move to higher ground immediately.",
                    "Keep emergency supplies ready.",
                    "Switch off electricity if safe.",
                    "Follow official advisories."
                ),
                donts = listOf(
                    "Do not walk through moving water.",
                    "Do not drive through flooded roads.",
                    "Do not touch electrical equipment in water."
                ),
                emergencyNumber = "1078"
            ),

            // EARTHQUAKE
            SafetyTip(
                title = "Earthquake Safety",
                category = "Earthquake",
                shortDescription = "Drop, Cover and Hold.",
                description = "Protect yourself during an earthquake by following standard safety procedures.",
                dos = listOf(
                    "Drop to the ground.",
                    "Take cover under sturdy furniture.",
                    "Hold on until shaking stops.",
                    "Move to open areas after the earthquake."
                ),
                donts = listOf(
                    "Do not use elevators.",
                    "Do not stand near windows.",
                    "Do not run during shaking."
                ),
                emergencyNumber = "112"
            ),

            // WOMEN SAFETY
            SafetyTip(
                title = "Women Safety",
                category = "Women Safety",
                shortDescription = "Personal safety and emergency helplines.",
                description = "Stay aware of your surroundings and contact authorities whenever you feel unsafe.",
                dos = listOf(
                    "Share your live location with trusted contacts.",
                    "Call Women Helpline (1091).",
                    "Stay in well-lit public places.",
                    "Trust your instincts."
                ),
                donts = listOf(
                    "Do not share personal details with strangers.",
                    "Do not ignore threatening behaviour.",
                    "Do not travel alone in unsafe areas if avoidable."
                ),
                emergencyNumber = "1091"
            ),

            // CHILD SAFETY
            SafetyTip(
                title = "Child Safety",
                category = "Child Safety",
                shortDescription = "Essential safety guidance for children.",
                description = "Teach children basic safety rules and ensure they know how to seek help during emergencies.",
                dos = listOf(
                    "Teach children emergency numbers.",
                    "Keep children supervised.",
                    "Teach them to seek help from trusted adults.",
                    "Keep emergency contacts updated."
                ),
                donts = listOf(
                    "Do not leave young children unattended.",
                    "Do not allow children near hazardous areas.",
                    "Do not ignore child safety concerns."
                ),
                emergencyNumber = "1098"
            )

        )
    }
}