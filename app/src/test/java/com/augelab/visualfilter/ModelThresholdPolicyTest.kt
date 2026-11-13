package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class ModelThresholdPolicyTest {
 @Test fun oldModelCustomThresholdIsNotSilentlyReused(){val x=ModelThresholdPolicy.choose(null,.6699382f);assertEquals(BinaryModelContract.THRESHOLD,x.value,0f);assertTrue(x.initializeCurrentModel);assertTrue(x.migratedFromLegacy)}
 @Test fun freshInstallUsesSmall224DefaultWithoutMigrationNotice(){val x=ModelThresholdPolicy.choose(null,null);assertEquals(.4477543234825134f,x.value,0f);assertTrue(x.initializeCurrentModel);assertFalse(x.migratedFromLegacy)}
 @Test fun laterUpgradesPreserveSmall224Customization(){val x=ModelThresholdPolicy.choose(.42f,.6699382f);assertEquals(.42f,x.value,0f);assertFalse(x.initializeCurrentModel);assertFalse(x.migratedFromLegacy)}
 @Test fun invalidCurrentSettingFallsBackToSmall224InsteadOfLegacy(){val x=ModelThresholdPolicy.choose(Float.NaN,.9f);assertEquals(BinaryModelContract.THRESHOLD,x.value,0f);assertTrue(x.initializeCurrentModel)}
 @Test fun keysAreBoundToExactFineTunedExport(){assertEquals("threshold_mobilenetv3small224_review_e202e26c",BinaryModelContract.THRESHOLD_KEY);assertEquals(224,BinaryModelContract.SIZE);assertEquals("e202e26cf35d3445d00ac7af74183d1d962121b3a5327fc41afd63605d5c4780",BinaryModelContract.SHA256)}
}
