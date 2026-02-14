package com.brahos.app.data.local

import androidx.room.TypeConverter
import com.brahos.app.domain.model.RiskLevel

class RiskLevelConverter {
    @TypeConverter
    fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = RiskLevel.valueOf(value)
}
