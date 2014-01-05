package com.eyecall.event;

import com.eyecall.connection.Named;
import com.eyecall.eventbus.Event;

public class SurfaceCreatedEvent extends Event {

        public SurfaceCreatedEvent(Named named) {
                super(named);
        }

}