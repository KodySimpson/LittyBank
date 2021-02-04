package me.kodysimpson.littybank.menu;

import me.kodysimpson.littybank.models.AccountTier;
import me.kodysimpson.littybank.models.SavingsAccount;
import me.kodysimpson.simpapi.menu.AbstractPlayerMenuUtility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerMenuUtility extends AbstractPlayerMenuUtility {

    private Location atmLocation;
    private AccountTier tier;
    private String accountName;

    public PlayerMenuUtility(Player p) {
        super(p);
    }

    public Location getAtmLocation() {
        return atmLocation;
    }

    public void setAtmLocation(Location atmLocation) {
        this.atmLocation = atmLocation;
    }

    public AccountTier getTier() {
        return tier;
    }

    public void setTier(AccountTier tier) {
        this.tier = tier;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
