package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the converter service
 */
class ConverterImpl extends ConverterGrpc.ConverterImplBase
{
    // list of length, weight, and temperature units
    private final ArrayList<String> LENGTH_UNITS;
    private final ArrayList<String> WEIGHT_UNITS;
    private final ArrayList<String> TEMPERATURE_UNITS;

    /**
     * Constructor
     */
    public ConverterImpl() 
    {
        super();
        LENGTH_UNITS = new ArrayList<>();
        WEIGHT_UNITS = new ArrayList<>();
        TEMPERATURE_UNITS = new ArrayList<>();

        // add length units
        LENGTH_UNITS.add("KILOMETER");
        LENGTH_UNITS.add("MILE");
        LENGTH_UNITS.add("YARD");
        LENGTH_UNITS.add("FOOT");
        // add weight units
        WEIGHT_UNITS.add("KILOGRAM");
        WEIGHT_UNITS.add("POUND");
        // add temperature units
        TEMPERATURE_UNITS.add("CELSIUS");
        TEMPERATURE_UNITS.add("FAHRENHEIT");
    }


    @Override
    public void convert(ConversionRequest request, StreamObserver<ConversionResponse> responseObserver)
    {
        System.out.println("Received from client: " +request.getValue()+ " " +request.getFromUnit()+ " to " +request.getToUnit());
        ConversionResponse.Builder response = ConversionResponse.newBuilder();
        // trim and convert to uppercase units
        String from = request.getFromUnit().trim().toUpperCase();
        String to = request.getToUnit().trim().toUpperCase();

        // no from_unit given
        if (from.isEmpty())
        {
            response.setIsSuccess(false).setError("from_unit is required");
        }
        // no to_unit given
        else if (to.isEmpty())
        {
            response.setIsSuccess(false).setError("to_unit is required");
        }
        // unsupported from_unit given
        else if (!isValidUnit(from))
        {
            response.setIsSuccess(false).setError("unsupported unit: " +from);
        }
        // unsupported to_unit given
        else if (!isValidUnit(to))
        {
            response.setIsSuccess(false).setError("unsupported unit: " +to);
        }
        // same units given
        else if (from.equals(to))
        {
            response.setIsSuccess(false).setError("same unit - no conversion needed");
        }
        // invalid conversion given
        else if (!isValidConversion(from, to))
        {
            response.setIsSuccess(false).setError("units do not match - cannot convert " +from+ " to " +to);
        }
        // temperature below absolute zero given
        else if (((from.equals("CELSIUS") && request.getValue() < -273.15)) || ((from.equals("FAHRENHEIT") && request.getValue() < -459.67)))
        {
            response.setIsSuccess(false).setError("temperature below absolute zero (-273.15°C or -459.67°F)");
        }
        // valid conversion given
        else
        {
            double result = roundTwoDecimals(convertUnits(request.getValue(), from, to));
            response.setIsSuccess(true).setResult(result);
        }
    
        ConversionResponse r = response.build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }


    /**
     * Check if the unit is valid
     */
    public boolean isValidUnit(String unit)
    {
        return LENGTH_UNITS.contains(unit) || WEIGHT_UNITS.contains(unit) || TEMPERATURE_UNITS.contains(unit);
    }


    /**
     * Check if the conversion is valid for from and to units
     */
    public boolean isValidConversion(String from, String to)
    {
        if ((LENGTH_UNITS.contains(from)) && (LENGTH_UNITS.contains(to)))
        {
            return true;
        }
        else if ((WEIGHT_UNITS.contains(from)) && (WEIGHT_UNITS.contains(to)))
        {
            return true;
        }
        else if ((TEMPERATURE_UNITS.contains(from)) && (TEMPERATURE_UNITS.contains(to)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * Convert a value from one unit to other
     */
    public double convertUnits(double value, String from, String to)
    {
        // check if temperature conversion
        if (TEMPERATURE_UNITS.contains(from))
        {
            // convert celsius to fahrenheit
            if (from.equals("CELSIUS"))
            {
                return (value*9.0/5.0) + 32.0;
            }
            // convert fahrenheit to celsius
            else 
            {
                return (value - 32.0) * 5.0 / 9.0;
            }
        }

        // check if weight conversion
        else if (WEIGHT_UNITS.contains(from))
        {
            // convert kilogram to pounds
            if (from.equals("KILOGRAM"))
            {
                return (value * 2.20462);
            }
            // conver pounds to kilograms
            else
            {
                return (value / 2.20462);
            }
        }

        // length conversion
        else
        {
            double valueMeters = toMeters(value, from);
            return fromMeters(valueMeters, to);
        }
    }

    /**
     * Convert length value to meters
     */
    public double toMeters(double value, String unit)
    {
        // convert from km to meters
        if (unit.equals("KILOMETER"))
        {
            return (value * 1000.0);
        }
        // convert from miles to meters
        if (unit.equals("MILE"))
        {
            return (value * 1609.34);
        }
        // convert from yards to meters
        if (unit.equals("YARD"))
        {
            return (value * 0.9144);
        }
        // convert from feet to meters
        if (unit.equals("FOOT"))
        {
            return (value * 0.3048);
        }
        // meters
        return value;
    }


    /**
     * Convert length value from meters to unit given
     */
    private double fromMeters(double value, String unit)
    {
        // convert from meters to km
        if (unit.equals("KILOMETER"))
        {
            return (value / 1000.0);
        }
        // convert from meters to miles
        if (unit.equals("MILE"))
        {
            return (value / 1609.34);
        }
        // convert from meters to yards
        if (unit.equals("YARD"))
        {
            return (value / 0.9144);
        }
        // convert from meters to feet
        if (unit.equals("FOOT"))
        {
            return (value / 0.3048);
        }
        // meters
        return value;
    }


    /**
     * Round value to two decimal places
     */
    public double roundTwoDecimals(double value)
    {
        return new java.math.BigDecimal(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
