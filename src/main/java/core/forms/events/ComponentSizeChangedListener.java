package core.forms.events;

import java.util.EventListener;

public interface ComponentSizeChangedListener extends EventListener {
	void handleEvent(ComponentSizeChanged event);
}
