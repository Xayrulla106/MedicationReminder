package com.example.medicationreminder.data

import com.example.medicationreminder.data.local.entity.MedicationEntity

/**
 * Hardcoded initial treatment schedule (the 4 required medications).
 * IDs are explicit so alarm PendingIntent request codes stay stable.
 */
object SeedData {

    val MEDICATIONS: List<MedicationEntity> = listOf(

        MedicationEntity(
            id = 1,
            name = "Grandaxin",
            dosage = "50 mg",
            quantity = "1 tab",
            hour = 10,
            minute = 0,
            instructions = "Take after breakfast with water.",
            category = "TABLET",
            components = emptyList(),
            startDay = 1,
            durationDays = 30,
            isEnabled = true
        ),

        MedicationEntity(
            id = 2,
            name = "IV & Injections Session",
            dosage = "L-Lysine, Neuroxidol, Cortexin, Vitamin B6",
            quantity = "2 IV infusions + 2 IM injections",
            hour = 11,
            minute = 0,
            instructions = "Clinical procedure: 2 IV infusions + 2 IM injections. " +
                "Must take after a meal. (IV phase: 7 days, Injection phase: 10 days)",
            category = "IV_SESSION",
            components = listOf(
                "L-Lysine (IV)",
                "Neuroxidol (IV)",
                "Cortexin (IM)",
                "Vitamin B6 (IM)"
            ),
            startDay = 1,
            durationDays = 10, // Injection phase bounds the whole session
            isEnabled = true
        ),

        MedicationEntity(
            id = 3,
            name = "Fevarin",
            dosage = "50 mg",
            quantity = "1/2 tab",
            hour = 20,
            minute = 0,
            instructions = "Take after dinner with water. Do not chew.",
            category = "TABLET",
            components = emptyList(),
            // Delayed start: active ONLY from Day 5 onward.
            startDay = 5,
            durationDays = 26, // Day 5 .. Day 30
            isEnabled = true
        ),

        MedicationEntity(
            id = 4,
            name = "Rotalud",
            dosage = "2 mg",
            quantity = "1 tab",
            hour = 21,
            minute = 30,
            instructions = "Take right before bedtime to relax muscles.",
            category = "TABLET",
            components = emptyList(),
            startDay = 1,
            durationDays = 20, // Day 1 .. Day 20
            isEnabled = true
        )
    )
}
