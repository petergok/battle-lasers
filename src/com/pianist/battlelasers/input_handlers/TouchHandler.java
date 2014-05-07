package com.pianist.battlelasers.input_handlers;

import java.util.ArrayList;
import java.util.List;

import com.pianist.battlelasers.Pool;
import com.pianist.battlelasers.Pool.PoolObjectFactory;
import com.pianist.battlelasers.input_handlers.Input.TouchEvent;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * The TouchHandler class handles all touch events and stores them in custom TouchEvent objects
 * 
 * @author "Apress Beginning Android Games"
 * @version June, 2011
 */
public class TouchHandler implements OnTouchListener
{
	private static final int MAX_TOUCHPOINTS = 10;

	boolean[] isTouched = new boolean[MAX_TOUCHPOINTS];

	int[] touchX = new int[MAX_TOUCHPOINTS];

	int[] touchY = new int[MAX_TOUCHPOINTS];

	int[] id = new int[MAX_TOUCHPOINTS];

	Pool<TouchEvent> touchEventPool;

	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();

	List<TouchEvent> touchEventsBuffer = new ArrayList<TouchEvent>();

	float scaleX;

	float scaleY;

	public TouchHandler(View view, float scaleX, float scaleY)
	{
		PoolObjectFactory<TouchEvent> factory = new PoolObjectFactory<TouchEvent>() {
			public TouchEvent createObject()
			{
				return new TouchEvent();
			}
		};
		touchEventPool = new Pool<TouchEvent>(factory, 100);
		view.setOnTouchListener(this);
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	public synchronized boolean onTouch(View v, MotionEvent event)
	{
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		int pointerCount = event.getPointerCount();
		TouchEvent touchEvent;

		for (int i = 0; i < MAX_TOUCHPOINTS; i++)
		{
			if (i >= pointerCount)
			{
				isTouched[i] = false;
				id[i] = -1;
				continue;
			}
			int pointerId = event.getPointerId(i);
			if (event.getAction() != MotionEvent.ACTION_MOVE
					&& i != pointerIndex)
			{
				// if it's an up/down/cancel/out event, mask the id to see if we
				// should process it for this touch point
				continue;
			}

			switch (action)
			{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				touchEvent = touchEventPool.newObject();
				touchEvent.type = TouchEvent.TOUCH_DOWN;
				touchEvent.pointer = pointerId;
				touchEvent.x = touchX[i] = (int) (event.getX(i) * scaleX);
				touchEvent.y = touchY[i] = (int) (event.getY(i) * scaleY);
				isTouched[i] = true;
				id[i] = pointerId;
				touchEventsBuffer.add(touchEvent);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				touchEvent = touchEventPool.newObject();
				touchEvent.type = TouchEvent.TOUCH_UP;
				touchEvent.pointer = pointerId;
				touchEvent.x = touchX[i] = (int) (event.getX(i) * scaleX);
				touchEvent.y = touchY[i] = (int) (event.getY(i) * scaleY);
				isTouched[i] = false;
				id[i] = -1;
				touchEventsBuffer.add(touchEvent);
				break;
			case MotionEvent.ACTION_MOVE:
				touchEvent = touchEventPool.newObject();
				touchEvent.type = TouchEvent.TOUCH_DRAGGED;
				touchEvent.pointer = pointerId;
				touchEvent.x = touchX[i] = (int) (event.getX(i) * scaleX);
				touchEvent.y = touchY[i] = (int) (event.getY(i) * scaleY);
				isTouched[i] = true;
				id[i] = pointerId;
				touchEventsBuffer.add(touchEvent);
				break;
			}
		}
		return true;
	}

	public synchronized boolean isTouchDown(int pointer)
	{
		int index = getIndex(pointer);
		if (index < 0 || index >= MAX_TOUCHPOINTS)
			return false;
		else
			return isTouched[index];

	}

	public synchronized int getTouchX(int pointer)
	{
		int index = getIndex(pointer);
		if (index < 0 || index >= MAX_TOUCHPOINTS)
			return 0;
		else
			return touchX[index];

	}

	public synchronized int getTouchY(int pointer)
	{

		int index = getIndex(pointer);
		if (index < 0 || index >= MAX_TOUCHPOINTS)
			return 0;
		else
			return touchY[index];

	}

	public synchronized List<TouchEvent> getTouchEvents()
	{

		int len = touchEvents.size();
		for (int i = 0; i < len; i++)
			touchEventPool.free(touchEvents.get(i));
		touchEvents.clear();
		touchEvents.addAll(touchEventsBuffer);
		touchEventsBuffer.clear();
		return touchEvents;

	}

	private int getIndex(int pointerId)
	{
		for (int i = 0; i < MAX_TOUCHPOINTS; i++)
		{
			if (id[i] == pointerId)
			{
				return i;
			}
		}
		return -1;
	}
}