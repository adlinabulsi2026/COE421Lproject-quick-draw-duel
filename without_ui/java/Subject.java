// Subject interface (Observer design pattern).
// The ZigBeeReader is the subject: it receives data and tells the observers.
public interface Subject {
	public void registerObserver(Observer o);
	public void removeObserver(Observer o);
	public void notifyObservers();
}
