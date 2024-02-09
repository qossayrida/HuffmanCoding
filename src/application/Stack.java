package application;

public class Stack {
    private int maxSize;
    private TreeNode[] stackArray;
    private int top;

    // Constructor to initialize the stack
    public Stack(int size) {
        maxSize = size;
        stackArray = new TreeNode[maxSize];
        top = -1;
    }

    // Method to add an item to the stack
    public void push(TreeNode value) {
        if (isFull()) {
            System.out.println("The stack is full. Cannot push " + value);
        } else {
            stackArray[++top] = value;
        }
    }

    // Method to remove an item from the stack
    public TreeNode pop() {
        if (isEmpty()) {
            return null;
        } else {
            return stackArray[top--];
        }
    }

    // Method to peek at the top item of the stack
    public TreeNode peek() {
        if (isEmpty()) {
            System.out.println("The stack is empty. Cannot peek.");
            return null;
        } else {
            return stackArray[top];
        }
    }

    // Method to check if the stack is empty
    public boolean isEmpty() {
        return (top == -1);
    }

    // Method to check if the stack is full
    public boolean isFull() {
        return (top == maxSize - 1);
    }
    
}