package cn.link.lis;

import java.util.ArrayList;

public class Level {
    static ArrayList<Level> sLevels;

    static {
        sLevels = gen();
    }

    int level;
    String name;
    int count_num;
    int max_num;
    int text_size;
    int gold;

    public Level(int level, String name, int count_num, int max_num, int text_size, int gold) {
        this.level = level;
        this.name = name;
        this.count_num = count_num;
        this.max_num = max_num;
        this.text_size = text_size;
        this.gold = gold;
    }

    public static Level firstLevel() {
        return sLevels.get(0);
    }

    public Level nextLevel() {
        return sLevels.get(level + 1);
    }

    public static ArrayList<Level> gen() {
        ArrayList<Level> levels = new ArrayList<Level>();
        levels.add(new Level(0, "幼稚园小朋友", 5, 8, 50, 1));
        levels.add(new Level(1, "小学生", 8, 9, 50, 2));
        levels.add(new Level(2, "中学生", 16, 49, 50, 4));
        levels.add(new Level(3, "高中生", 20, 69, 40, 16));
        levels.add(new Level(4, "大学生", 24, 99, 30, 32));
        levels.add(new Level(5, "研究僧", 32, 199, 25, 64));
        levels.add(new Level(6, "博士僧", 48, 199, 20, 128));
        levels.add(new Level(7, "女博士后", 64, 199, 14, 512));
        levels.add(new Level(8, "处女座外星人", 99, 1999, 12, 1024));
        return levels;
    }
}