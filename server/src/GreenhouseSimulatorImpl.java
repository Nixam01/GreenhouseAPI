import java.util.Calendar;
import java.util.Date;

/**
 * Klasa symulująca działanie szklarnii.
 * Autor: Jan Ignasiak 311001
 *
 * Posiada kilka zmiennych prywatnych, które przechowują wartości symulowane.
 * Posiada metody z określonymi formułami generujące symulowane wartości w zależności od wykonanych operacji:
 *
 * Temperatura jest opisana funkcją powolni opadającą gdy nie jest włączone ogrzewanie aż do stałej temperatury zewnętrznej,
 * wokół której będzie się ona utrzymywać, natomiast w przypadku włączenia ogrzewania zacznie ona rosnąć do tempeartury maksymalnej
 * w okolicy 35 stopni, gdzie też będzie się ona utrzymywać aż do wyłączenia ogrzewnia.
 *
 * Wilgotność gleby jest opisana funkcją, która maleje powoli zależnie od natężenia światła i obecnej temperatury - woda powoli odparowuje,
 * natomiast w wypadku włączenia podlewania zacznie ona gwałtownie rosnąć aż do maksymalnej wartości 50,
 * gdzie większość czujników przestaje rozróżniać wyższe wartości i wyżej ziemia przestaje być możliwa do utrzymania.
 *
 * Naświetlenie jest opisane odpowiednią funkcją kwadratową opisującą przykłądowy dzień,
 * gdzie w zależności od obecnego czasu zostanie pokazana przykłądowa wartość nasłonecznienia.
 * W przypadku włączenia oświetlenia, to utrzymuje się na stałym poziomie.
 *
 * Następnie klasa implementuje metody z interfejsu, które umożliwiają łatwe podłączenie jej do serwera.
 */
public class GreenhouseSimulatorImpl implements GreenhouseSimulator {
    private long delay;
    private boolean heating;
    private double currentTemperature;
    private final double OUTSIDE_TEMPERATURE = 20.0;
    private boolean watering;
    private double currentWatering;
    private boolean light;
    private double currentLightLevel;

    public GreenhouseSimulatorImpl() {
        delay = 2000L;
        heating = false;
        watering = false;
        light = false;
        currentTemperature = OUTSIDE_TEMPERATURE;
        currentWatering = 3.0;
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        double minutes = cal.get(Calendar.MINUTE) / 12.0;
        if (hour > 6 && hour <= 14) {
            currentLightLevel = (hour - 6) * 10.0 + minutes;
        } else if (hour > 14 && hour <= 22) {
            currentLightLevel = (23 - hour) * 10.0 + minutes;
        } else {
            currentLightLevel = 2.0 * Math.random();
        }
    }

    private double getTemperature() {
        if (!heating || (currentTemperature > 35)) {
            currentTemperature -= Math.random();
        }
        if (currentTemperature < (OUTSIDE_TEMPERATURE-1.5)) {
            currentTemperature++;
        }
        currentTemperature += Math.random();
        return currentTemperature;
    }

    private double getSoilMoisture() {
        if(watering && currentWatering < 50.0) {
            currentWatering += (2.0*Math.random());
        } else if(watering && currentWatering >=50) {
            currentWatering -= 1.0;
            currentWatering += Math.random();
        } else if(currentWatering > 0.2) {
            currentWatering -= (currentTemperature/25.0 * Math.random());
            currentWatering -= (currentLightLevel/200.0 * Math.random());
            if(currentWatering < 0.3)
                currentWatering = Math.random()/3.0;
        } else
            currentWatering = Math.random()/2.0;
        return currentWatering;
    }

    private double getLightLevel() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        double minutes = cal.get(Calendar.MINUTE) / 12.0;
        if (hour > 4 && hour < 21) {
            currentLightLevel = 25.0 * (hour-5) - 1.5625*(hour-5)*(hour-5) - 10.0 + minutes + Math.random();
        } else {
            currentLightLevel = 3.0 * Math.random();
        }
        if(light && currentLightLevel < 90.0) {
            currentLightLevel = 90.0 + Math.random();
        }
        return currentLightLevel;
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Override
    public String getData() {
        return String.format("Temperature: %.2f°C. Soil moisture: %.2f. Light level: %.1f%%.",getTemperature(),getSoilMoisture(),getLightLevel());
    }

    @Override
    public void toggleFunction(String dataType) {
        switch (dataType) {
            case GreenhouseProtocol.HEATING ->
                heating = !heating;
            case GreenhouseProtocol.WATERING ->
                watering = !watering;
            case GreenhouseProtocol.LIGHT ->
                light = !light;
        }
    }

}
