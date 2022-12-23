/**
 * Interfejs do obsługi symulatora w serwerze.
 * Autorzy: Jan Ignasiak
 */
public interface GreenhouseSimulator {

    long getDelay();

    void setDelay(long delay);

    String getData();

    void toggleFunction(String dataType);

}
