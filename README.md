# WebSocketsRails client port for Android

Port of JavaScript client provided by https://github.com/websocket-rails/websocket-rails

Built on top of AndroidAsync

## Misc

Refer to https://github.com/websocket-rails/websocket-rails to learn more about WebSocketRails

Refer to https://github.com/koush/AndroidAsync to learn more about AndroidAsync

## Download

Download [the latest JAR](https://search.maven.org/remote_content?g=br.net.bmobile&a=websocketrails-android&v=LATEST
) or grab via Maven:

```xml
<dependency>
    <groupId>br.net.bmobile</groupId>
    <artifactId>websocketrails-android</artifactId>
    <version>(insert latest version)</version>
</dependency>
```

Gradle: 
```groovy
dependencies {
    compile 'br.net.bmobile:websocketrails-android:1.+'
}
```

## Example

Since data exchange is JSON based, it's strongly recommended to use Jackson
API to deserialize data.

Here's an example:

```
private WebSocketRailsDispatcher dispatcher;

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	dispatcher = new WebSocketRailsDispatcher("http://192.168.100.109:3000/websocket");

	Button button = (Button) findViewById(R.id.sendButton);
	
	button.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			WebSocketRailsChannel channel = dispatcher.subscribe("my_channel");
			Notification notification = new Notification();
			notification.setType(1);
			notification.setContent("");
			
			Contact contact = new Contact();
			contact.setIdentifier(7);
			contact.setFirstName("Bond");
			contact.setLastName("");
			contact.setCountryCode("+1");
			contact.setPhoneNumber("777000007");
			
			notification.setContact(contact);
			channel.trigger("notification_event", notification);
			
			channel.bind("notification_event", new WebSocketRailsCallback() {
				@Override
				public void onDataAvailable(Object data) {
					
					ObjectMapper mapper = new ObjectMapper();
					if(data instanceof Map) {
			
						notification = (Notification) mapper.convertValue(data, new TypeReference<Notification>(){});
						
						//do something						
					}
				}		
			});
		}
	});
}
```