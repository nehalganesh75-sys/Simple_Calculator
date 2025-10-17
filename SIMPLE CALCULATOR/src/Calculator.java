import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Stack;
import javax.swing.*;

public class Calculator extends JFrame implements ActionListener, KeyListener {
    private JTextField display;
    private StringBuilder expression = new StringBuilder();
    private boolean startNewNumber = true;
    private final DecimalFormat df = new DecimalFormat("0.########");

    public Calculator() {
        setTitle("Simple Calculator");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(10, 10));

        display = new JTextField("0");
        display.setFont(new Font("Segoe UI", Font.BOLD, 32));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setEditable(false);
        display.setBackground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(display, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(6, 4, 10, 10));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel, BorderLayout.CENTER);

        String[] buttons = {
                "CE", "C", "⌫", "/",
                "√", "7", "8", "9",
                "*", "4", "5", "6",
                "-", "1", "2", "3",
                "+", "+/-", "0", ".",
                "%", "="
        };

        for (String text : buttons) {
            JButton btn = createButton(text);
            panel.add(btn);
        }

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btn.setFocusPainted(false);
        btn.setBackground(isOperator(text) ? new Color(100, 149, 237) : Color.WHITE);
        btn.setForeground(isOperator(text) ? Color.WHITE : Color.BLACK);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(isOperator(text) ? new Color(65, 105, 225) : new Color(240, 240, 240));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(isOperator(text) ? new Color(100, 149, 237) : Color.WHITE);
            }
        });
        btn.addActionListener(this);
        return btn;
    }

    private boolean isOperator(String text) {
        return "/-*+=C⌫CE√%".contains(text);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        handleInput(e.getActionCommand());
        requestFocusInWindow();
    }

    private void handleInput(String command) {
        try {
            switch (command) {
                case "C":
                    expression.setLength(0);
                    display.setText("0");
                    startNewNumber = true;
                    break;
                case "CE":
                    clearLastNumber();
                    break;
                case "⌫":
                    if (expression.length() > 0) {
                        expression.deleteCharAt(expression.length() - 1);
                        if (expression.length() == 0) expression.append("0");
                        display.setText(expression.toString());
                    }
                    break;
                case "+/-":
                    toggleSign();
                    break;
                case "√":
                    applySqrt();
                    break;
                case "%":
                    applyPercent();
                    break;
                case "=":
                    calculate();
                    break;
                default:
                    if (isMathOperator(command)) appendOperator(command);
                    else appendNumber(command);
                    break;
            }
        } catch (Exception ex) {
            display.setText("Error");
            expression.setLength(0);
            startNewNumber = true;
        }
    }

    private void appendNumber(String num) {
        if (startNewNumber && !num.equals(".")) {
            expression.append(num);
            startNewNumber = false;
        } else {
            int lastOp = lastOperatorIndex();
            String lastNum = expression.substring(lastOp + 1);
            if (num.equals(".") && lastNum.contains(".")) return;
            expression.append(num);
        }
        display.setText(expression.toString());
    }

    private void appendOperator(String op) {
        if (expression.length() == 0) return;
        char lastChar = expression.charAt(expression.length() - 1);
        if ("+-*/".indexOf(lastChar) != -1) {
            expression.setCharAt(expression.length() - 1, op.charAt(0));
        } else {
            expression.append(op);
        }
        display.setText(expression.toString());
        startNewNumber = true;
    }

    private void clearLastNumber() {
        int lastOp = lastOperatorIndex();
        expression.delete(lastOp + 1, expression.length());
        if (expression.length() == 0) expression.append("0");
        display.setText(expression.toString());
        startNewNumber = true;
    }

    private int lastOperatorIndex() {
        int lastOp = -1;
        for (char c : new char[]{'+','-','*','/'}) {
            int idx = expression.lastIndexOf(String.valueOf(c));
            if (idx > lastOp) lastOp = idx;
        }
        return lastOp;
    }

    private void toggleSign() {
        try {
            if (expression.length() == 0) return;
            int i = expression.length() - 1;
            while (i >= 0 && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                i--;
            }
            if (i >= 0 && expression.charAt(i) == '-') {
                if (i == 0 || "+-*/".indexOf(expression.charAt(i - 1)) != -1) {
                    i--;
                }
            }

            String number = expression.substring(i + 1);
            if (number.isEmpty()) return;

            double val = Double.parseDouble(number);
            val = -val;

            expression.replace(i + 1, expression.length(), df.format(val));
            display.setText(expression.toString());
            startNewNumber = true;
        } catch (Exception e) {
            display.setText("Error");
            expression.setLength(0);
            startNewNumber = true;
        }
    }

    private void applySqrt() {
        try {
            int lastOp = lastOperatorIndex();
            String number = expression.substring(lastOp + 1);
            double val = Double.parseDouble(number);
            if (val < 0) throw new Exception();
            expression.replace(lastOp + 1, expression.length(), df.format(Math.sqrt(val)));
            display.setText(expression.toString());
            startNewNumber = true;
        } catch (Exception e) {
            display.setText("Error");
            expression.setLength(0);
            startNewNumber = true;
        }
    }

    private void applyPercent() {
        try {
            int lastOp = lastOperatorIndex();
            String number = expression.substring(lastOp + 1);
            double val = Double.parseDouble(number);
            expression.replace(lastOp + 1, expression.length(), df.format(val / 100));
            display.setText(expression.toString());
        } catch (Exception e) { display.setText("Error"); expression.setLength(0); }
    }

    private boolean isMathOperator(String s) {
        return "+-*/".contains(s);
    }

    private void calculate() {
        try {
            if (expression.length() == 0) return;
            double result = evaluateExpression(expression.toString());
            String resStr = df.format(result);
            display.setText(resStr);
            expression.setLength(0);
            expression.append(resStr);
            startNewNumber = true;
        } catch (Exception e) {
            display.setText("Error");
            expression.setLength(0);
            startNewNumber = true;
        }
    }

    private double evaluateExpression(String expr) throws Exception {
        Stack<Double> nums = new Stack<>();
        Stack<Character> ops = new Stack<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                nums.push(Double.parseDouble(sb.toString()));
            } else if ("+-*/".indexOf(c) != -1) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(c)) {
                    double b = nums.pop();
                    double a = nums.pop();
                    nums.push(applyOp(a, b, ops.pop()));
                }
                ops.push(c);
                i++;
            } else throw new Exception("Invalid char");
        }
        while (!ops.isEmpty()) {
            double b = nums.pop();
            double a = nums.pop();
            nums.push(applyOp(a, b, ops.pop()));
        }
        return nums.pop();
    }

    private int precedence(char op) {
        return (op == '+' || op == '-') ? 1 : 2;
    }

    private double applyOp(double a, double b, char op) throws Exception {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': if (b == 0) throw new Exception("Div by 0"); return a / b;
        }
        throw new Exception("Unknown op");
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        char key = e.getKeyChar();
        int code = e.getKeyCode();
        if (Character.isDigit(key)) handleInput(String.valueOf(key));
        else if (key == '.') handleInput(".");
        else if ("+-*/".indexOf(key) != -1) handleInput(String.valueOf(key));
        else if (code == KeyEvent.VK_ENTER || key == '=') handleInput("=");
        else if (code == KeyEvent.VK_BACK_SPACE) handleInput("⌫");
        else if (code == KeyEvent.VK_DELETE) handleInput("CE");
        else if (code == KeyEvent.VK_ESCAPE) handleInput("C");
        else if (key == '%') handleInput("%");
        else if (key == 'r' || key == 'R') handleInput("√");
        else if (key == 'n' || key == 'N') handleInput("+/-");
    }
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculator calc = new Calculator();
            calc.setVisible(true);
        });
    }
}