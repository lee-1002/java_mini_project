
package mygame;

public class Warrior extends Character {
    public Warrior() {
        super("Warrior", 150, 50, 20,
              "https://png.pngtree.com/png-clipart/20240420/original/pngtree-powerful-viking-warrior-graphic-for-tshirt-png-image_14911528.png");
    }

    @Override
    protected void initializeSkills() {
        skills.put("Slash", new Skill("Slash", 2, 4, "2x Damage", "slash"));
        skills.put("Berserk", new Skill("Berserk", 3, 2, "3x Damage + Recoil", "berserk"));
        skills.put("Double Strike", new Skill("Double Strike", 1, 3, "Attack Twice", "double"));
        skills.put("Healing Strike", new Skill("Healing Strike", 1, 2, "Damage + Heal", "heal"));
    }

    @Override
    public int useSkill(Skill skill) {
        if (mp < skill.getMpCost()) return 0;

        mp -= skill.getMpCost();
        int damage = attack * skill.getDamageMultiplier();

        switch(skill.getName()) {
            case "Berserk":
                hp = Math.max(0, hp - (int)(maxHp * 0.1));
                break;
            case "Healing Strike":
                hp = Math.min(maxHp, hp + (int)(maxHp * 0.3));
                break;
            case "Double Strike":
                damage *= 2;
                break;
        }
        return damage;
    }
}
