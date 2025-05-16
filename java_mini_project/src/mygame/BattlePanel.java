package mygame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URL;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class BattlePanel extends JPanel {
    private Character player;
    private List<Monster> monsters = new ArrayList<>();
    private int monsterIndex = 0;
    private Monster monster;
    private BufferedImage playerImage;
    private BufferedImage monsterImage;
    private BufferedImage backgroundImage;
    private BufferedImage slashEffectImage;
    private int stage = 1;

    // 애니메이션 상태
    private boolean isPlayerAttacking = false;
    private int animationStep = 0;
    private static final int TOTAL_STEPS = 20;
    private static final int HALF_STEPS  = TOTAL_STEPS / 2;
    private Runnable onAnimationComplete;
    private Timer attackTimer;

    // 피격 플래시
    private boolean playerFlashing  = false;
    private boolean monsterFlashing = false;

    // Slash 이펙트
    private boolean slashActive = false;
    private int slashX, slashY;
    private int slashVelocity = 20;
    private Timer slashTimer;

    // 캐릭터 크기 & 위치
    private static final int CHAR_WIDTH          = 120;
    private static final int CHAR_HEIGHT         = 180;
    private static final int PLAYER_BASE_X       = 100;
    private static final int MONSTER_BASE_OFFSET = 220;

    // 플로팅 텍스트
    private String floatingText = "";
    private Timer floatingTextTimer;

    public BattlePanel() {
        setPreferredSize(new Dimension(1000, 500));
        loadBackground();
        loadSlashEffectImage();
        setFocusable(true);
        setupKeyBindings();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // 일반 공격 (SPACE)
        im.put(KeyStroke.getKeyStroke("pressed SPACE"), "attack");
        am.put("attack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                }
        });

        // Slash (Q)
        im.put(KeyStroke.getKeyStroke("pressed Q"), "slash");
        am.put("slash", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (monster == null || slashActive || monster.hp <= 0) return;
                Character.Skill skill = player.getSkills().get("Slash");
                if (!skill.canUse(player.getMp())) {
                    showFloatingText("No MP");
                    return;
                }
                playSlashEffect(() -> useSkill(skill));
            }
        });

        // Berserk (W)
        im.put(KeyStroke.getKeyStroke("pressed W"), "berserk");
        am.put("berserk", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (monster == null || isPlayerAttacking || monster.hp <= 0) return;
                Character.Skill skill = player.getSkills().get("Berserk");
                if (!skill.canUse(player.getMp())) {
                    showFloatingText("No MP");
                    return;
                }
                playAttackAnimation(true, skill.getName(), () -> useSkill(skill));
            }
        });

        // Double Strike (E)
        im.put(KeyStroke.getKeyStroke("pressed E"), "doubleStrike");
        am.put("doubleStrike", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (monster == null || isPlayerAttacking || monster.hp <= 0) return;
                Character.Skill skill = player.getSkills().get("Double Strike");
                if (!skill.canUse(player.getMp())) {
                    showFloatingText("No MP");
                    return;
                }
                playAttackAnimation(true, skill.getName(), () -> useSkill(skill));
            }
        });

        // Healing Strike (R)
        im.put(KeyStroke.getKeyStroke("pressed R"), "healStrike");
        am.put("healStrike", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (player == null || player.hp <= 0 || player.hp >= player.getMaxHp()) return;
                Character.Skill skill = player.getSkills().get("Healing Strike");
                if (!skill.canUse(player.getMp())) {
                    showFloatingText("No MP");
                    return;
                }
                playAttackAnimation(true, skill.getName(), () -> useSkill(skill));
            }
        });
    }

    private void loadBackground() {
        try {
            backgroundImage = ImageIO.read(new URL(
                "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyMjExMDFfMjE5%2FMDAxNjY3MjY5OTY3NTA0.Xsyf8waRu6oil_PN445C5OsgoBk1E9IgEIXUIwR0cygg.jAeLafx--6gJqvhzWsiC9wOTNjL7fj-RK2G5ODgUxXYg.PNG.mo7933%2F%257B%257B%257B%257B%257BApocalypse%257D%257D%257D%257D%257D%252C_ruin%252C_modern%252C_masterpiece%252C_%257B%257B%257B%257B%257Bbest_quality%257D%257D%257D%257D%257D%252C__de.png&type=sc960_832"));
        } catch (Exception e) {
            backgroundImage = null;
        }
    }

    private void loadSlashEffectImage() {
        try {
            slashEffectImage = ImageIO.read(new URL(
                "https://blog.kakaocdn.net/dn/nhhfj/btqJTGDnz3s/p2IPA1eT1VqrJikJTtLz60/img.gif"));
        } catch (Exception e) {
            slashEffectImage = null;
        }
    }

    /** 캐릭터와 첫 몬스터 설정 (Game 클래스에서 호출) */
    public void setCharacters(Character player, Monster firstMonster) {
        this.player       = player;
        this.monsters.clear();
        this.monsters.add(firstMonster);
        this.monsterIndex = 0;
        this.monster      = firstMonster;
        playerImage       = loadImage(player.getImagePath());
        monsterImage      = loadImage(monster.imagePath);
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new URL(path));
        } catch (Exception e) {
            BufferedImage img = new BufferedImage(CHAR_WIDTH, CHAR_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, CHAR_WIDTH, CHAR_HEIGHT);
            g.setColor(Color.BLACK);
            g.drawString("No Image", 30, 90);
            g.dispose();
            return img;
        }
    }

    public void playAttackAnimation(boolean isPlayerAttack, String text, Runnable onComplete) {
        this.isPlayerAttacking   = isPlayerAttack;
        this.onAnimationComplete = onComplete;
        this.animationStep       = 0;
        showFloatingText(text);
        if (attackTimer != null) attackTimer.stop();
        attackTimer = new Timer(20, e -> {
            animationStep++;
            if (animationStep == HALF_STEPS) {
                if (isPlayerAttacking) monsterFlashing = true;
                else                   playerFlashing  = true;
            }
            if (animationStep == HALF_STEPS + 2) {
                monsterFlashing = playerFlashing = false;
            }
            if (animationStep >= TOTAL_STEPS) {
                attackTimer.stop();
                animationStep    = 0;
                isPlayerAttacking = false;
                if (onAnimationComplete != null) {
                    onAnimationComplete.run();
                }
            }
            repaint();
        });
        attackTimer.start();
    }

    public void playSlashEffect(Runnable onComplete) {
        this.onAnimationComplete = onComplete;
        slashActive              = true;
        slashX                   = PLAYER_BASE_X + CHAR_WIDTH;
        slashY                   = getHeight() / 2 - CHAR_HEIGHT / 4;
        showFloatingText("Slash");
        if (slashTimer != null) slashTimer.stop();
        slashTimer = new Timer(30, e -> {
            slashX += slashVelocity;
            if (slashX > getWidth() - MONSTER_BASE_OFFSET) {
                slashActive = false;
                slashTimer.stop();
                if (onAnimationComplete != null) {
                    onAnimationComplete.run();
                }
            }
            repaint();
        });
        slashTimer.start();
    }

    /** 스킬 사용(mp 차감 & 데미지/힐 처리) */
    private void useSkill(Character.Skill skill) {
        int result = player.useSkill(skill);
        if (skill.getName().equals("Healing Strike")) {
            showFloatingText("Healed");
        } else {
            monster.hp = Math.max(monster.hp - result, 0);
            if (monster.hp == 0) {
                showFloatingText("Defeated");
                new Timer(1500, ev -> loadNextMonster()) {{ setRepeats(false); }}.start();
                repaint();
                return;
            } else {
                playAttackAnimation(false, "Attack", this::applyDamageToPlayer);
            }
        }
        repaint();
    }

    /** 다음 몬스터 로드 및 스테이지별 HP 배수 적용 */
    private void loadNextMonster() {
        monsterIndex++;
        if (monsterIndex < monsters.size()) {
            monster = monsters.get(monsterIndex);
        } else {
            monster = Monster.getRandomMonster();
        }
        // 스테이지 증가
        stage++;
        // **스테이지만큼 최대체력 배수 증가** & 풀 회복
        monster.maxHp = monster.maxHp * stage;
        monster.hp    = monster.maxHp;
        player.hp     = player.getMaxHp();
        player.mp     = player.getMaxMp();

        monsterImage = loadImage(monster.imagePath);
        showFloatingText("Stage " + stage + " Start");
        repaint();
    }

    /** 몬스터 반격 */
    private void applyDamageToPlayer() {
        int dmg = monster.attack;
        player.hp = Math.max(player.hp - dmg, 0);
        showFloatingText(player.hp == 0 ? "Game Over" : "Hit!");
        repaint();
    }

    /** 플로팅 텍스트 */
    private void showFloatingText(String text) {
        floatingText = text;
        if (floatingTextTimer != null) floatingTextTimer.stop();
        floatingTextTimer = new Timer(1500, e -> { floatingText = ""; repaint(); });
        floatingTextTimer.setRepeats(false);
        floatingTextTimer.start();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 배경
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Stage 표시
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("Stage: " + stage, getWidth()/2 - 40, 40);

        // 위치 계산
        int px = PLAYER_BASE_X;
        int mx = getWidth() - MONSTER_BASE_OFFSET;
        int y  = getHeight()/2 - CHAR_HEIGHT/2;

        // 애니메이션 오프셋
        if (animationStep > 0 && animationStep < TOTAL_STEPS) {
            int md   = mx - px - CHAR_WIDTH;
            int dist = animationStep <= HALF_STEPS
                     ? animationStep * md / HALF_STEPS
                     : (TOTAL_STEPS - animationStep) * md / HALF_STEPS;
            if (isPlayerAttacking) px += dist; else mx -= dist;
        }

        // 캐릭터 & 몬스터 그리기
        if (playerImage  != null) g2.drawImage(playerImage,  px, y, CHAR_WIDTH, CHAR_HEIGHT, null);
        if (monsterImage != null) g2.drawImage(monsterImage, mx, y, CHAR_WIDTH, CHAR_HEIGHT, null);

        // 피격 플래시
        if (playerFlashing)  { g2.setColor(new Color(255,0,0,100)); g2.fillRect(px,y,CHAR_WIDTH,CHAR_HEIGHT); }
        if (monsterFlashing) { g2.setColor(new Color(255,0,0,100)); g2.fillRect(mx,y,CHAR_WIDTH,CHAR_HEIGHT); }

        // HP/MP 바
        if (player != null) {
            int ph=player.hp, pH=player.getMaxHp(), pm=player.mp, pM=player.getMaxMp();
            float pr=(float)ph/pH;
            g2.setColor(Color.GRAY); g2.fillRect(px,y-20,CHAR_WIDTH,10);
            g2.setColor(Color.RED);  g2.fillRect(px,y-20,(int)(CHAR_WIDTH*pr),10);
            g2.setColor(Color.WHITE);g2.drawRect(px,y-20,CHAR_WIDTH,10);
            g2.setColor(Color.GRAY); g2.fillRect(px,y-8,CHAR_WIDTH,6);
            g2.setColor(Color.BLUE); g2.fillRect(px,y-8,(int)(CHAR_WIDTH*((float)pm/pM)),6);
            g2.setColor(Color.WHITE);g2.drawRect(px,y-8,CHAR_WIDTH,6);
            g2.drawString("HP:" + ph + "/" + pH, px, y+CHAR_HEIGHT+20);
            g2.drawString("MP:" + pm + "/" + pM, px, y+CHAR_HEIGHT+40);
        }

        if (monster != null) {
            int mh=monster.hp, mH=monster.maxHp;
            float mr=(float)mh/mH;
            g2.setColor(Color.GRAY); g2.fillRect(mx,y-20,CHAR_WIDTH,10);
            g2.setColor(Color.RED);  g2.fillRect(mx,y-20,(int)(CHAR_WIDTH*mr),10);
            g2.setColor(Color.WHITE);g2.drawRect(mx,y-20,CHAR_WIDTH,10);
            g2.drawString("HP:" + mh + "/" + mH, mx, y+CHAR_HEIGHT+20);
        }

        // 플로팅 텍스트
        if (!floatingText.isEmpty()) {
            g2.setColor(Color.BLACK);
            g2.fillRoundRect(getWidth()/2 - 100, 80, 200, 40, 15, 15);
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString(floatingText,
                getWidth()/2 - g2.getFontMetrics().stringWidth(floatingText)/2,
                108
            );
        }

        // Slash 이펙트
        if (slashActive && slashEffectImage != null) {
            g2.drawImage(slashEffectImage, slashX, slashY, CHAR_WIDTH, CHAR_HEIGHT/2, null);
        }
    }
}
