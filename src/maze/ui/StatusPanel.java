package maze.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * 状态面板
 * 
 * 实时显示搜索运行状态与数据结构信息：
 *   当前状态、算法名称、Stack 大小、Queue 大小、
 *   已访问节点数、路径长度、运行耗时。
 */
public class StatusPanel extends JPanel {

    // ============ 标签 ============
    private final JLabel stateLabel;
    private final JLabel algoLabel;
    private final JLabel stackLabel;
    private final JLabel queueLabel;
    private final JLabel visitedLabel;
    private final JLabel pathLabel;
    private final JLabel timeLabel;

    // ============ 构造方法 ============

    public StatusPanel() {
        setLayout(new GridBagLayout());
        setBorder(new TitledBorder("运行状态"));
        setPreferredSize(new Dimension(300, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 列 0：标签名，列 1：数值
        stateLabel   = addRow("当前状态:", "就绪",       0, gbc);
        algoLabel    = addRow("算　　法:", "—",          1, gbc);
        stackLabel   = addRow("Stack 大小:", "0",        2, gbc);
        queueLabel   = addRow("Queue 大小:", "0",        3, gbc);
        visitedLabel = addRow("已访问节点:", "0",        4, gbc);
        pathLabel    = addRow("路径长度:",   "0",        5, gbc);
        timeLabel    = addRow("运行耗时:",   "0 ms",     6, gbc);

        // 底部撑开
        gbc.gridy = 7;
        gbc.weighty = 1;
        add(Box.createVerticalGlue(), gbc);
    }

    /**
     * 添加一行 "标签 : 值"。
     */
    private JLabel addRow(String label, String initial, int row, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel value = new JLabel(initial);
        value.setFont(value.getFont().deriveFont(Font.BOLD));
        add(value, gbc);

        return value;
    }

    // ============ 更新方法 ============

    public void setState(String text)     { stateLabel.setText(text); }
    public void setAlgorithm(String text) { algoLabel.setText(text); }
    public void setStackSize(int n)       { stackLabel.setText(String.valueOf(n)); }
    public void setQueueSize(int n)       { queueLabel.setText(String.valueOf(n)); }
    public void setVisited(int n)         { visitedLabel.setText(String.valueOf(n)); }
    public void setPathLength(int n)      { pathLabel.setText(String.valueOf(n)); }
    public void setElapsedTime(long ms)   { timeLabel.setText(ms + " ms"); }

    /**
     * 重置全部字段。
     */
    public void reset() {
        setState("就绪");
        setAlgorithm("—");
        setStackSize(0);
        setQueueSize(0);
        setVisited(0);
        setPathLength(0);
        setElapsedTime(0);
    }
}