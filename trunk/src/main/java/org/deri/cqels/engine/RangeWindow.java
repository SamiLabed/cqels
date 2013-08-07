package org.deri.cqels.engine;

import java.util.Timer;

import org.deri.cqels.lang.cqels.Duration;
import org.deri.cqels.lang.cqels.DurationSet;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
/** 
 * This class implements the time-based window 
 * @author		Danh Le Phuoc
 * @author 		Chan Le Van
 * @organization DERI Galway, NUIG, Ireland  www.deri.ie
 * @email 	danh.lephuoc@deri.org
 * @email   chan.levan@deri.org
 */
public class RangeWindow implements Window {
	Database buff;
    long w;
    long slide;
    long wInMili;
    long sInMili;
    long lastTimestamp = -1;
    Timer timer;
    public RangeWindow( long w) {
    	this.w = w;
		timer = new Timer();    	
    }
    
    public RangeWindow(long w, long slide) {
    	this.w = w; 
    	this.slide = slide;
    	this.wInMili = (long)(this.w / 1E6);
		this.sInMili = (long)(this.slide / 1E6);
		timer = new Timer();    	
    }
	
	public RangeWindow(DurationSet durations, Duration slideDuration) {
    	this.w = durations.inNanoSec();
    	this.wInMili = (long)(this.w / 1E6);
    	if(slideDuration != null) {
    		slide = slideDuration.inNanosec();
    		this.sInMili = (long)(this.slide / 1E6);
    	}
	}
	
	public void enableSlidePurifier(Purifier p) {
		if (wInMili < sInMili) {
			timer.schedule(p, wInMili, sInMili);
		}
		else {
			timer.schedule(p, sInMili, sInMili);
		}
	}
	
	public void setBuff(Database db) { 
		buff = db;
	}
	
	public long getSlide() {
		return (long)(this.slide);
	}
	
	public long getDuration() {
		return (long)(this.w);
	}
	
	public void purge(long timeRange, String message) {
		long curTime = System.nanoTime();
		//System.out.println(message + ", lasttime: " + lastTimestamp + " time range: " + timeRange + " curTime - time Range: " + (curTime - timeRange));
		if(lastTimestamp > 0 && lastTimestamp < curTime - timeRange) {
			//System.out.println(message + ": actually purge 2, buffercode: " + buff.hashCode());
			synchronized (buff) {
				Cursor cursor = buff.openCursor(null, CursorConfig.DEFAULT);
				DatabaseEntry key = new DatabaseEntry();
				DatabaseEntry data = new DatabaseEntry();
				long tmp;
				while(cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					//System.out.println(message + ": actually purge 3");
					tmp = LongBinding.entryToLong(key);
					if(tmp < (curTime - timeRange)) {
						//System.out.println(message + ": actually purge 4");
						cursor.delete();
						//System.out.println("purge" +lastTimestamp + " cur"+curTime+ " "+(curTime-w));
					}
					else {
						cursor.close();
						lastTimestamp = tmp;
						report(curTime);
						return;
					}
				}
				cursor.close();
				lastTimestamp = -1;
			}
		}
		report(curTime);		
	}
	
	public synchronized void purge() {
		String message = "purge by DURATION";
		purge(this.w, message);
	}
	public void reportLatestTime(long t) {
		if(lastTimestamp < 0) {
			lastTimestamp = t;
		}
	}
	
	public void report(long t){
		IndexedTripleRouter.accT += System.nanoTime() - t;
	}

	public RangeWindow clone() {
		RangeWindow w = new RangeWindow(this.getDuration(), this.getSlide());
		w.lastTimestamp = this.lastTimestamp;
		return w;
	}
}
