package io.github.wall69.ancientnightmare.utils;

import io.github.wall69.ancientnightmare.Main;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.connections.MineSkinAPI;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Random;

public class SkinUtils {

    private final SkinsRestorer skinsRestorerAPI;
    private final Random random;

    private SkinProperty wardenSkin, securityMaleSkin, securityFemaleSkin;

    public SkinUtils(Main main, SkinsRestorer skinsRestorerAPI) {
        this.skinsRestorerAPI = skinsRestorerAPI;
        this.random = new Random();

        setupWardenSkin(main);
        setupSecurityMaleSkin(main);
        setupSecurityFemaleSkin(main);
    }

    /*
        METHODS
     */

    public void setupWardenSkin(Main main) {
        this.wardenSkin = SkinProperty.of(main.getFileUtils().getWardenSkinValue(),
                main.getFileUtils().getWardenSkinSignature());
    }

    public void setupSecurityMaleSkin(Main main) {
        this.securityMaleSkin = SkinProperty.of(main.getFileUtils().getSecurityMaleSkinValue(),
                main.getFileUtils().getSecurityMaleSkinSignature());
    }

    public void setupSecurityFemaleSkin(Main main) {
        this.securityFemaleSkin = SkinProperty.of(main.getFileUtils().getSecurityFemaleSkinValue(),
                main.getFileUtils().getSecurityFemaleSkinSignature());
    }

    public void applySecuritySkin(Player player) {
        if (random.nextBoolean()) {
            skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, securityMaleSkin);
        } else {
            skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, securityFemaleSkin);
        }
    }

    public void applyWardenSkin(Player player) {
        skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, wardenSkin);
    }

    public void clearSkin(Player player) throws DataRequestException {
        skinsRestorerAPI.getPlayerStorage().removeSkinIdOfPlayer(player.getUniqueId());
        skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player);
    }

    public void setWardenSkin(Main main, String url) throws MineSkinException, DataRequestException {
        MineSkinAPI mineSkinAPI = skinsRestorerAPI.getMineSkinAPI();
        MineSkinResponse mineSkinResponse = mineSkinAPI.genSkin(url, null);

        main.getFileUtils().setWardenSkin(mineSkinResponse.getProperty().getValue(),
                mineSkinResponse.getProperty().getSignature());
        setupWardenSkin(main);
    }

    public void setSecuritySkin(Main main, String url, String who) throws MineSkinException, DataRequestException {
        MineSkinAPI mineSkinAPI = skinsRestorerAPI.getMineSkinAPI();
        MineSkinResponse mineSkinResponse = mineSkinAPI.genSkin(url, null);

        if (who.equals("male")) {
            main.getFileUtils().setSecurityMaleSkin(mineSkinResponse.getProperty().getValue(),
                    mineSkinResponse.getProperty().getSignature());
            setupSecurityMaleSkin(main);
        } else {
            main.getFileUtils().setSecurityFemaleSkin(mineSkinResponse.getProperty().getValue(),
                    mineSkinResponse.getProperty().getSignature());
            setupSecurityFemaleSkin(main);
        }
    }

    // DEFAULTS

    public String getDefaultWardenValue() {
        return "ewogICJ0aW1lc3RhbXAiIDogMTYxNjM5NTUwMzk0MSwKICAicHJvZmlsZUlkIiA6ICJiNzQ3OWJhZTI5YzQ0YjIzYmE1NjI4MzM3OGYwZTNjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTeWxlZXgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTdmOGZmMThhZTVmMmFhMzc0NTdmYzU4MzAzMzAwYTc1Mzk3MGM0NDg4Y2MwYWQ2ZTUyZDBjMDMwODY1MGUzIgogICAgfQogIH0KfQ==";
    }

    public String getDefaultWardenSignature() {
        return "xla/pXTv1Tuw5ISXiwBlxt9lQ4T2hCfXgBHTNdkOy52AP+KN5tEiKQ3eEVYTexSRv0OqggzxmtiiaPC508Kxi39Grmr8WQiimZZ0210+lqEGStrU++NbDnKh/+OWY73JiUQdQDBY4AmxLAq1WjqanWiLFIWfXYMwRvcbzgqAn5dIeAHEKfTzzi7kdygYZeOp+WohENmvExMr+wFmlm2lUylXNnR65LFvLlUs/sfHEqhiaol2HcVxwVg2B7pzqeFHTUgFwFEROzpvYlQLNZv/NSq2a6vA4xCk7pGlAUvDjSCI9XzQLFrRaBzqfhWheuOHaDkhpbBtu2E7m49zIRWTR4scMseZyuDc9jghAHpmL/fii4oQCBKBtW5g29NLQUSCHP2n817MbNhRki+q+M/TzqOoQIX6kaVu7WKQO+mUf04CtUkGVBwK7AexXwj8xHMcbKiihCK3FBwRvH3ZRbHgYIPl3HPzp4j2XS6rHoU2dobMoOP+6/UBo4Z6L9X1RdYuDJHYQwithv3PHbbx2qG0jfumHyKg2YxJyW10ShMqYAF9Lb298DKNvpWhKGiYSTigPFBODf4AixJPj/LH2Vb+oZHRtvQEK6QqZ8cK4lVXqYULAnmEsKnPEY2ZswdgjTMyXDpSjpXxwDpst3LDtvYTDZW1QlkRfCBzh48KOEwGmtk=";
    }

    public String getDefaultSecurityMaleValue() {
        return "eyJ0aW1lc3RhbXAiOjE1ODcwNTE5Nzk1MDksInByb2ZpbGVJZCI6ImQ2MGYzNDczNmExMjQ3YTI5YjgyY2M3MTViMDA0OGRiIiwicHJvZmlsZU5hbWUiOiJCSl9EYW5pZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ4NTcyZjExMDEyNWI1YmM3MDc4NmQzNzhhYWFhY2FiYjgwYzJlZDAwOTEyNWE4MmU5Y2FmYmEwMmRiODJmMjgifX19";
    }

    public String getDefaultSecurityMaleSignature() {
        return "MJsmm3wbsQspbDSI/iJjEqGnVnUrtLZuaWH5iXXcusW5mAAxbgl2VqZxS2EOu8NcH2giwxDVWYCx1j7VmTFvfK7CavsKtzYgCwsMmoVGR/lswXmK+3KcnOn9+YQ82y4FSb2PbjitKdh22YZ5RNNbTVyFrIHhXei/+BUGgx28KmVcJRXogsAvzq4COAFYlsa07+NtDJPr5lPKz+KzW90GSJLri4qtZkFG3BxCMZolnNwdOnop56mzdyx7hO4jgmCdXSJHlMnv1sOSTlXpfOSqDIxktSOGDLHmkkJNl1do8MbfbZaTlAJfAAqQYmQ7411Xz2jFag0LVFzVYtXqtFTr8m4Dh+kZ7ayP5qcUcXzj/C7r1aTvUIMCnrY5JmWwGdC2F1dgIkUhLY5dXRIpx+EMdi3JIiKIih2/BO3JfiNOLmk0BASrqXgagZuE5EVZaODwCpJDdnYfLdkM+dgdHQSLD+/7LNZsbeqCTNbw/Z09nqpyCQsiziqB8yUxAeQXT7iF1FbpSsbPtm/FJmqwty4u5XjlB59x8uhjvulVmzDt47qB59TpOqhc76C9yGqQnDObBhSohgDWQJ1T8k+1iNvLBauv+olBqBcKct13x/NGsJaWSoIVMw39EOxI1uO6c/5AejjWWi2wUaMoSGQ6zTZFyQQKFadWt/k7B5hsfj9pftk=";
    }

    public String getDefaultSecurityFemaleValue() {
        return "ewogICJ0aW1lc3RhbXAiIDogMTYwMzU4NDE2MDI4OSwKICAicHJvZmlsZUlkIiA6ICI2MTI4MTA4MjU5M2Q0OGQ2OWIzMmI3YjlkMzIxMGUxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJPbGROaWNrVGhlUmVhcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M3M2IyMDgwMTIyZmFhNzhjNWRjMTkxZDdmZWU5OWZiM2UwYjg3ODg0MTYzNWU1NTM2ZmQ0MDUxZGNiODRjYTgiCiAgICB9CiAgfQp9";
    }

    public String getDefaultSecurityFemaleSignature() {
        return "rr35iIrqjCzRbwv+vdRhf2RF2GI7DLaF5q8AcQzESalplT8gEnR667/HQR5ge18/DGbtv63QRr6DM8GuYQwgdz4wHdyfdLrHebzlAZ6BjNJj9rcljksxcY750UiG+s80cbVTOWXNiVW6KqMlaqbADa/B28V5BXs4LjIoxcmCvqm9EL5ywB4gNxqgJQhrK4sy90iVL2bgrlvt9kmqH86KHjvZn9RQkAn9amFKeJp6lhYbOD8crWvl2LwrNTzBYXYbtis3dBr1QJ7LtFP606QdKfJOJhExdGdGEnWpdlscc3fNH8q1YmPBLp9HpzjbcwvbUyhJ02PjXKhTvTfrl+cnFY4ewHkHLhCRcukAU5Iyfs2UBpp9H5dA/ES6b5nqfPpXPMQMQDehmidqr9H8srlaVRFkxyH2H+0MNpmD1uBZ+xumrdVpGgBqVVEtr8vPQqUMFfE6dMVliUQMvLSMdRkxJCXzkdpNEijT0s9vjqfEqDHdzPJW2Z6pyedLv6O+KnnB4MgL6yUIMEhunZfOZTpp6Ld/7c2b5K3/5Jk7569rI8AWIidvho1L3khuXgg5LHuIuO/Zvtk4tWpwqp/+4DXl8q43DdIlx/WCQgiY8nRM/UfhZDTwSHLfXsOSdfKMauHmtyMNJ3K6fQhMoW7PiabbFBKgNvNg/t16fVnfxZG/5zQ=";
    }

}