
package mygame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

public class Game extends JFrame {
    private Character player;
    private Monster currentMonster;
    private JTextArea logArea;
    private JButton attackButton;
    private JButton[] skillButtons = new JButton[4];
    private BattlePanel battlePanel;
    private int stage = 1;

    public Game(Character player) {
        this.player = player;
        setupWindow();
        initComponents();
        startNewBattle();
        setVisible(true);
    }

    private void setupWindow() {
        setTitle("RPG Battle System");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(60, 60, 90));
    }

    private void initComponents() {
        // 메인 전투 화면
        battlePanel = new BattlePanel();
        battlePanel.setStage(stage);

        // 로그 창
        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        logArea.setForeground(Color.WHITE);
        logArea.setBackground(new Color(40, 40, 60));
        logArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        JScrollPane logScroll = new JScrollPane(logArea);

        // 스킬 버튼
        JPanel skillPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        skillPanel.setBackground(new Color(60, 60, 90));
        Map<String, Character.Skill> skills = player.getSkills();
        String[] skillNames = skills.keySet().toArray(new String[0]);
        for (int i = 0; i < skillButtons.length && i < skillNames.length; i++) {
            String skillName = skillNames[i];
            skillButtons[i] = new JButton(skills.get(skillName).getStatus());
            skillButtons[i].addActionListener(e -> useSkill(skillName));
            styleButton(skillButtons[i]);
            skillPanel.add(skillButtons[i]);
        }

        // 일반 공격 버튼
        attackButton = new JButton("Attack");
        attackButton.addActionListener(e -> attack());
        styleButton(attackButton);

        // 레이아웃 배치
        add(battlePanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(60, 60, 90));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(new Color(60, 60, 90));
        buttonPanel.add(skillPanel, BorderLayout.NORTH);
        buttonPanel.add(attackButton, BorderLayout.SOUTH);

        bottomPanel.add(logScroll, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(80, 80, 120));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 140), 2));
        button.setPreferredSize(new Dimension(120, 40));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(100, 100, 140));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(80, 80, 120));
            }
        });
    }

    private void startNewBattle() {
        currentMonster = Monster.getRandomMonster();
        battlePanel.setCharacters(player, currentMonster);
        log("Stage " + stage + ": A wild " + currentMonster.name + " appears!");
        player.hp = player.maxHp;
        player.restoreMp(5 + stage);
        log(player.name + "'s HP and MP have been restored!");
        updateUI();
    }

    private void updateUI() {
        battlePanel.repaint();
        updateSkillButtons();
    }

    private void updateSkillButtons() {
        Map<String, Character.Skill> skills = player.getSkills();
        String[] names = skills.keySet().toArray(new String[0]);
        for (int i = 0; i < skillButtons.length && i < names.length; i++) {
            Character.Skill skill = skills.get(names[i]);
            skillButtons[i].setText(skill.getStatus());
            skillButtons[i].setEnabled(skill.canUse(player.mp));
            skillButtons[i].setToolTipText(skill.getDescription());
        }
    }

    private void useSkill(String skillName) {
        Character.Skill skill = player.getSkills().get(skillName);
        if (!skill.canUse(player.mp)) {
            log("Not enough MP to use " + skillName);
            return;
        }
        // 스킬 사용 애니메이션 후 데미지 적용
        if ("Slash".equals(skill.getName())) {
            battlePanel.playSlashEffect(() -> applySkillDamage(skill));
        } else {
            battlePanel.playAttackAnimation(true, skill.getEffectType(),
                    () -> applySkillDamage(skill));
        }
    }

    private void applySkillDamage(Character.Skill skill) {
        int damage = player.useSkill(skill);
        currentMonster.hp = Math.max(0, currentMonster.hp - damage);
        log(player.name + " uses " + skill.getName() + " for " + damage + " damage!");
        if (currentMonster.hp <= 0) {
            log(currentMonster.name + " defeated!");
            stage++;
            battlePanel.setStage(stage);
            startNewBattle();
        } else {
            // 몬스터 반격
            battlePanel.playAttackAnimation(false, "Attack", () -> {
                monsterCounterAttack();
                updateUI();
            });
        }
        updateUI();
    }

    private void attack() {
        // 일반 공격 애니메이션 후 데미지 적용
        battlePanel.playAttackAnimation(true, "Attack", () -> {
            int damage = player.attack;
            currentMonster.hp = Math.max(0, currentMonster.hp - damage);
            log(player.name + " attacks for " + damage + " damage!");
            if (currentMonster.hp <= 0) {
                log(currentMonster.name + " defeated!");
                stage++;
                battlePanel.setStage(stage);
                startNewBattle();
            } else {
                battlePanel.playAttackAnimation(false, "Attack", () -> {
                    monsterCounterAttack();
                    updateUI();
                });
            }
            updateUI();
        });
    }

    private void monsterCounterAttack() {
        int damage = currentMonster.attack;
        if (player instanceof Tanker) {
            Tanker t = (Tanker) player;
            if (t.isDefending()) {
                log(player.name + " blocks the attack!");
                t.resetDefense();
                return;
            }
            if (t.isTaunting()) {
                damage = t.getReducedDamage(damage);
            }
        }
        player.hp = Math.max(0, player.hp - damage);
        log(currentMonster.name + " counterattacks for " + damage + " damage!");
        if (player.hp <= 0) {
            log(player.name + " has been defeated! Game Over!");
            attackButton.setEnabled(false);
            for (JButton btn : skillButtons) btn.setEnabled(false);
        }
    }

    private void log(String msg) {
        logArea.append("> " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
