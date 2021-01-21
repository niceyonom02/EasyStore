public interface PriceObservable {
    public void register(PriceObserver observer);
    void unregister(PriceObserver observer);
    void notifyChange();
}
