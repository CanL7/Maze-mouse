package maze.ui;

import maze.util.StepType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志面板
 * 
 * 实时显示搜索过程中的关键事件。
 * 每条日志带时间戳，自动滚动到最新。
 * 
 * 日志格式：
 *   [HH:mm:ss.SSS] 操作描述
 */
public class LogPanel extends JPanel {

    /** 时间戳格式 */
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /** 日志输出区域（只读） */
    private final JTextArea textArea;

    // ============ 构造方法 ============

    public LogPanel() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("搜索日志"));
        setPreferredSize(new Dimension(300, 140));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ============ 公共方法 ============

    /**
     * 追加一条日志。
     *
     * @param message 日志内容（不含时间戳，会自动添加）
     */
    public void append(String message) {
        String timestamp = LocalTime.now().format(TIME_FMT);
        textArea.append(String.format("[%s] %s%n", timestamp, message));
        // 自动滚动到底部
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    /**
     * 追加一条搜索步骤日志。
     *
     * @param stepNumber 步骤序号
     * @param row        格子行
     * @param col        格子列
     * @param type       步骤类型
     */
    public void appendStep(int stepNumber, int row, int col, StepType type) {
        String action;
        switch (type) {
            case VISIT     -> action = String.format("访问 (%d,%d)", row, col);
            case BACKTRACK -> action = String.format("回溯 (%d,%d) ← 死路", row, col);
            case FOUND     -> action = String.format("★★★ 找到一条可行路径，终点 (%d,%d)！ ★★★", row, col);
            case SHORTEST_PATH -> action = String.format("最短路径已确定，终点 (%d,%d)", row, col);
            default        -> action = "未知操作";
        }
        append(String.format("步骤 %d — %s", stepNumber, action));
    }

    /**
     * 清空所有日志。
     */
    public void clear() {
        textArea.setText("");
    }
}
