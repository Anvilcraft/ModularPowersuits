package net.machinemuse.powersuits.powermodule.movement;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.machinemuse.api.IModularItem;
import net.machinemuse.api.ModuleManager;
import net.machinemuse.api.moduletrigger.IPlayerTickModule;
import net.machinemuse.api.moduletrigger.IToggleableModule;
import net.machinemuse.general.sound.SoundDictionary;
import net.machinemuse.numina.basemod.NuminaConfig;
import net.machinemuse.numina.sound.Musique;
import net.machinemuse.powersuits.control.PlayerInputMap;
import net.machinemuse.powersuits.item.ItemComponent;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.utils.ElectricItemUtils;
import net.machinemuse.utils.MuseCommonStrings;
import net.machinemuse.utils.MuseItemUtils;
import net.machinemuse.utils.MusePlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class JetPackModule extends PowerModuleBase implements IToggleableModule, IPlayerTickModule {
    public static final String MODULE_JETPACK = "Jetpack";
    public static final String JET_ENERGY_CONSUMPTION = "Jetpack Energy Consumption";
    public static final String JET_THRUST = "Jetpack Thrust";

    public JetPackModule(List<IModularItem> validItems) {
        super(validItems);
        addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.ionThruster, 4));
        addBaseProperty(JET_ENERGY_CONSUMPTION, 0, "J/t");
        addBaseProperty(JET_THRUST, 0, "N");
        addTradeoffProperty("Thrust", JET_ENERGY_CONSUMPTION, 150);
        addTradeoffProperty("Thrust", JET_THRUST, 0.16);
    }

    @Override
    public String getCategory() {
        return MuseCommonStrings.CATEGORY_MOVEMENT;
    }

    @Override
    public String getDataName() {
        return MODULE_JETPACK;
    }

    @Override
    public String getUnlocalizedName() { return "jetpack";
    }

    @Override
    public String getDescription() {
        return "A jetpack should allow you to jump indefinitely, or at least until you run out of power.";
    }

    @Override
    public void onPlayerTickActive(EntityPlayer player, ItemStack item) {
        if (player.isInWater()) {
            return;
        }
        PlayerInputMap movementInput = PlayerInputMap.getInputMapFor(player.getCommandSenderName());
        boolean jumpkey = movementInput.jumpKey;
        ItemStack helmet = player.getCurrentArmor(3);
        boolean hasFlightControl = helmet != null && helmet.getItem() instanceof IModularItem
                && ModuleManager.itemHasActiveModule(helmet, FlightControlModule.MODULE_FLIGHT_CONTROL);
        double jetEnergy = 0;
        double thrust = 0;
        jetEnergy += ModuleManager.computeModularProperty(item, JET_ENERGY_CONSUMPTION);
        thrust += ModuleManager.computeModularProperty(item, JET_THRUST);

        if (jetEnergy < ElectricItemUtils.getPlayerEnergy(player)) {
            thrust *= MusePlayerUtils.getWeightPenaltyRatio(MuseItemUtils.getPlayerWeight(player), 25000);
            if (hasFlightControl && thrust > 0) {
                thrust = MusePlayerUtils.thrust(player, thrust, true);
                if ((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) && NuminaConfig.useSounds()) {
                    Musique.playerSound(player, SoundDictionary.SOUND_JETPACK, (float) (thrust * 6.25), 1.0f, true);
                }
                ElectricItemUtils.drainPlayerEnergy(player, thrust * jetEnergy);
            } else if (jumpkey && player.motionY < 0.5) {
                thrust = MusePlayerUtils.thrust(player, thrust, false);
                if ((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) && NuminaConfig.useSounds()) {
                    Musique.playerSound(player, SoundDictionary.SOUND_JETPACK, (float) (thrust * 6.25), 1.0f, true);
                }
                ElectricItemUtils.drainPlayerEnergy(player, thrust * jetEnergy);
            } else {
                if ((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) && NuminaConfig.useSounds()) {
                    Musique.stopPlayerSound(player, SoundDictionary.SOUND_JETPACK);
                }
            }
        } else {
            if ((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) && NuminaConfig.useSounds()) {
                Musique.stopPlayerSound(player, SoundDictionary.SOUND_JETPACK);
            }
        }
    }

    @Override
    public void onPlayerTickInactive(EntityPlayer player, ItemStack item) {
        if ((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) && NuminaConfig.useSounds()) {
            Musique.stopPlayerSound(player, SoundDictionary.SOUND_JETPACK);
        }
    }

    @Override
    public String getTextureFile() {
        return "jetpack";
    }
}
