package com.toonystank.requisiteteams.gui.system.builder.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.system.components.exception.GuiException;
import com.toonystank.requisiteteams.gui.system.components.util.SkullUtil;
import com.toonystank.requisiteteams.gui.system.components.util.VersionHelper;
import com.toonystank.requisiteteams.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public final class SkullBuilder extends BaseItemBuilder<SkullBuilder> {

    private static final Field PROFILE_FIELD;

    static {
        Field field;

        try {
            final SkullMeta skullMeta = (SkullMeta) SkullUtil.skull().getItemMeta();
            field = skullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            field = null;
        }

        PROFILE_FIELD = field;
    }

    public SkullBuilder() {
        super(SkullUtil.skull());
    }

    SkullBuilder(final @NotNull ItemStack itemStack) {
        super(itemStack);
        if (!SkullUtil.isPlayerSkull(itemStack)) {
            throw new GuiException("SkullBuilder requires the material to be a PLAYER_HEAD/SKULL_ITEM!");
        }
    }

    /**
     * Sets the skull texture using a BASE64 string
     *
     * @param texture The base64 texture
     * @param profileId The unique id of the profile
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_, _ -> this")
    public SkullBuilder texture(@NotNull final String texture, @NotNull final UUID profileId) {
        if (!SkullUtil.isPlayerSkull(getItemStack())) {
            MessageUtils.debug("SkullBuilder requires the material to be a PLAYER_HEAD/SKULL_ITEM!");
            return this;
        }

        if (VersionHelper.IS_PLAYER_PROFILE_API) {
            MessageUtils.debug("Using PlayerProfile API for skull texture setting. texure is" + texture);
            final String textureUrl = SkullUtil.getSkinUrl(texture);

            if (textureUrl == null) {
                return this;
            }

            final SkullMeta skullMeta = (SkullMeta) getMeta();
            final PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "");
            final PlayerTextures textures = profile.getTextures();

            try {
                textures.setSkin(new URL(textureUrl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return this;
            }

            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
            setMeta(skullMeta);
            return this;
        }

        if (PROFILE_FIELD == null) {
            MessageUtils.debug("Profile field not found, falling back to legacy method.");
            return this;
        }
        MessageUtils.debug("Using legacy method for skull texture setting.");
        final SkullMeta skullMeta = (SkullMeta) getMeta();
        final GameProfile profile = new GameProfile(profileId, "");
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            MessageUtils.debug("Setting skull texture with GameProfile.");
            PROFILE_FIELD.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        MessageUtils.debug("Setting skull texture with GameProfile completed.");

        setMeta(skullMeta);
        return this;
    }

    /**
     * Sets the skull texture using a BASE64 string
     *
     * @param texture The base64 texture
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public SkullBuilder texture(@NotNull final String texture) {
        return texture(texture, UUID.randomUUID());
    }

    /**
     * Sets the skull owner using either SkinRestorer (if enabled in config and available)
     * or fallback Bukkit methods. If SkinRestorer is enabled and a skin is found,
     * the custom texture will be applied. Otherwise, uses standard Bukkit player assignment.
     *
     * @param player {@link OfflinePlayer} whose skin should be applied to the skull
     * @return {@link SkullBuilder} this builder instance for chaining
     * @throws GuiException if SkinRestorer is enabled but the skin cannot be found or retrieved
     */
    @NotNull
    @Contract("_ -> this")
    public SkullBuilder owner(@NotNull final RequisitePlayer player) {
        if (!SkullUtil.isPlayerSkull(getItemStack())) return this;

        final SkullMeta skullMeta = (SkullMeta) getMeta();

        skullMeta.setOwningPlayer(player.getPlayer());


        setMeta(skullMeta);
        return this;
    }


    /**
     * Sets the item to a mob head corresponding to the given EntityType.
     *
     * @param type The entity type (e.g., ZOMBIE, CREEPER)
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public SkullBuilder mob(@NotNull final EntityType type) {
        Material mobHead = getMobHeadMaterial(type);
        setItemStack(new ItemStack(mobHead));
        return this;
    }

    /**
     * Maps supported EntityTypes to their corresponding mob head Materials.
     *
     * @param type The entity type
     * @return Corresponding Material or null if not supported
     */
    private static Material getMobHeadMaterial(@NotNull EntityType type) {
        return switch (type) {
            case SKELETON -> Material.SKELETON_SKULL;
            case WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL;
            case ZOMBIE -> Material.ZOMBIE_HEAD;
            case CREEPER -> Material.CREEPER_HEAD;
            case ENDER_DRAGON -> Material.DRAGON_HEAD;
            case PIGLIN -> Material.PIGLIN_HEAD;
            default -> Material.PLAYER_HEAD;

        };
    }


}
