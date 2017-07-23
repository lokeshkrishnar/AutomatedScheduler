
package AutomatedScheduler;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;




public class ScheduleProcess
{
    static ScheduledExecutorService scheduler       = null;
    static ScheduledFuture< ? >     schedulerHandle = null;
    static Properties               properties      = new Properties();
    static InputStream              input;
    static String                   setMode;
    static String                   initialDelay;
    static String                   runDelay;
    static String                   runTime;
    static String                   timeUnit;
    static TimeUnit                 timeUnitValue;
    static String                   processPath;
    static long                     initialDelayValue;
    static long                     runDelayValue;
    static long                     runTimeValue;

    public static void main( String[] args )
    {
        Properties properties = new Properties();
        InputStream input;
        boolean init = false;
        try
        {
            PrintStream printStream = new TestPrintStream( new FileOutputStream("C:\\AutomatedLog.out") );
            System.setOut( printStream );
            System.setErr( printStream );
            input = new FileInputStream( "C:\\AutomatedTestService.properties" );
            properties.load( input );
            setMode = properties.getProperty( "setMode" );
            initialDelay = properties.getProperty( "initialDelay" );
            runDelay = properties.getProperty( "runDelay" );
            runTime = properties.getProperty( "runTime" );
            timeUnit = properties.getProperty( "timeUnit" );
            processPath = properties.getProperty( "processPath" );
            initialDelayValue = Long.parseLong( initialDelay );
            runDelayValue = Long.parseLong( runDelay );
            runTimeValue = Long.parseLong( runTime );
            if ( timeUnit.equalsIgnoreCase( "seconds" ) == true )
            {
                timeUnitValue = TimeUnit.SECONDS;
            }
            else if ( timeUnit.equalsIgnoreCase( "minutes" ) == true )
            {
                timeUnitValue = TimeUnit.MINUTES;
            }
            else if ( timeUnit.equalsIgnoreCase( "hours" ) == true )
            {
                timeUnitValue = TimeUnit.HOURS;
            }
            else if ( timeUnit.equalsIgnoreCase( "days" ) == true )
            {
                timeUnitValue = TimeUnit.DAYS;
            }

            if ( setMode.equalsIgnoreCase( "start" ) == true )
            {
                init = true;
                start( args );
            }
            else
                if(setMode.equalsIgnoreCase( "stop" ) == true || args.length>0)
                {
                    if("stop".equalsIgnoreCase( args[0] ))
                    {
                        stop(args);
                    }
                }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            if ( scheduler != null )
            {
                scheduler.shutdown();
            }
        }

    }

    public static void start( String[] args ) throws Exception
    {
        if ( scheduler == null )
        {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            schedulerHandle = scheduler.scheduleWithFixedDelay(
                    new ScheduleProcess.RunMainMethod(), initialDelayValue,
                    runDelayValue, timeUnitValue );
            scheduler.schedule( new Runnable()
            {
                public void run()
                {
                    schedulerHandle.cancel( true );
                }
            }, runTimeValue, timeUnitValue );
        }
      }

    public static void stop( String[] args ) throws Exception
    {
        if ( scheduler != null )
        {
            scheduler.shutdown();
        }
    }

    public static class RunMainMethod implements Runnable
    {

        @Override
        public void run()
        {
            try
            {
                CommandLine commandLine = CommandLine.parse( processPath );
                DefaultExecutor executor = new DefaultExecutor();
                executor.setExitValue( 0 );
                int exitValue = executor.execute( commandLine );
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public static class TestPrintStream extends PrintStream
    {
        public TestPrintStream( OutputStream out )
        {
            super( out );
        }
    }

}
