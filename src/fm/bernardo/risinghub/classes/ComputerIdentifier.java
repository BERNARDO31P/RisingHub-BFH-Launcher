package fm.bernardo.risinghub.classes;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.util.Arrays;

import static fm.bernardo.risinghub.classes.SHA512.sha512;


final class ComputerIdentifier
{

    static String generateLicenseKey ()
    {
        final SystemInfo systemInfo = new SystemInfo();
        final OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        final HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        final CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        final ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

        final String vendor = operatingSystem.getManufacturer(), processorSerialNumber = computerSystem.getSerialNumber(), processorIdentifier = centralProcessor.getIdentifier();
        final int processors = centralProcessor.getLogicalProcessorCount();

        final StringBuilder soundCards = new StringBuilder(), networkInterfaces = new StringBuilder();

        for (SoundCard card : hardwareAbstractionLayer.getSoundCards()) {
            soundCards.append(card.toString());
        }

        for (NetworkIF net : hardwareAbstractionLayer.getNetworkIFs()) {
            networkInterfaces.append(net.getName());
            networkInterfaces.append(net.getMacaddr());
            networkInterfaces.append(Arrays.toString(net.getIPv4addr()));
            networkInterfaces.append(Arrays.toString(net.getIPv6addr()));
        }

        String delimiter = "#";

        return sha512(vendor + delimiter + processorSerialNumber + delimiter + processorIdentifier + delimiter + processors + delimiter + soundCards + delimiter + networkInterfaces, "DarkCrypt");
    }

}