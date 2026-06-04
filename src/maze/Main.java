package maze;

import maze.ui.MainFrame;

import javax.swing.*;

/**
 * 程序入口
 * 
 * 启动 Swing 主窗口，使用 SwingUtilities.invokeLater 保证线程安全。
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}