package io.github.wall69.ancientnightmare.game;

public enum SpecialAbilityItem {

    WARDEN_SONIC_ATTACK("an_wardenSonicAttack"),
    WARDEN_STEALTH("an_wardenStealthAbility"),
    WARDEN_RAGE("an_wardenRageAbility"),
    WARDEN_COMPASS("an_wardenCompass"),

    SECURITY_APPLE("an_securityApple"),
    SECURITY_FAKE_SOUND("an_securityFakeSound"),
    SECURITY_BATTERY("an_securityBattery"),
    SECURITY_BATON("an_securityBaton");

    private final String localizedName;

    SpecialAbilityItem(String localizedName){
        this.localizedName = localizedName;
    }

    public String getLocalizedName(){
        return localizedName;
    }
}
