package edu.kpi.ipsa.opavloshchuk.airways.data;

/**
 * Рейс
 */
public class Flight {

    private int number; // номер. Унікальний ключ
    private int from; // аеропорт, звідки рейс починається
    private int to; // аеропорт, де рейс закінчується
    private int income; // прибуток від рейсу або пріорітет рейсу
    private int expenses; // витрати на рейс
    private int departureTime; // час відльоту
    private int arrivalTime; // час прильоту
    private boolean mandatory; // рейс є обов'язковим?

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public int getExpenses() {
        return expenses;
    }

    public void setExpenses(int expenses) {
        this.expenses = expenses;
    }

}
