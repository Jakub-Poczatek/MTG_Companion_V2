# MTG Companion V2
A magic the gathering companion app, second iteration.

## Functionality
This description of functionality will be broken down into sections and views.

### Login
The app supports 2 ways of login in and registering. The user can either authenticate with email and password or google. This authentication is handled by Firebase Auth.  
The user is also able to sign out, once authenticated.

### Card Details
The card details/add screen is used to add new and update existing cards. On this screen the user needs to enter the following information:  
* ``Card name`` via text input field
* ``Card type`` via combo box
* ``Card power/toughness`` via 2 seekbars
* ``Card colour costs`` via multiple number pickers
* ``card description`` via multi-line text input field
* ``card rarity`` via combo box
* ``card set`` via text input field
* ``card value`` via number input field
* ``card art`` via a image picker  

All card data is saved on a realtime firebase database. The card arts are saved in firebase object cloud storage.

### Card List
The card list screen will display a list of cards and offer some extra functionality. The user is able to do the following things:
* Display their own list of cards
* Display all of the cards by pressing the toggle on the top bar
* Enable/Disable dark mode via the moon icon on the top bar 
* Sort all cards in multiple ways via the sort icon on the top bar
* Filter through cards based on name and type
* Add a card by clicking on the floating action button with the plus icon

Once the list of cards is populated, the user can also click on any card and will be moved to the card detail screen. This time the screen will be populated with the cards data, the data can then be changed and saved. This will update the database.

### Map
The map screen will display a google maps centered around the user's current position, a purple marker is placed in the user's current position. Additionally the google places api is contacted to retrieve the closest 20 places the user can find new cards, a marker is placed on the location of each of these places. If a marker is interacted with, the bottom information will be displayed with the follow place information:
* Place name
* Place address
* Place latitude/longitude
* Place rating
* Place's amount of ratings
* Place's open/close status

Additionally to the aforementioned information, the user can press a directions button. This will contact the google directions api and retrieve a path between the user's current location and the chose place. 

## Nav Drawer
The nav drawer is used to switch between the aforementioned screens. From here the user can also click on their profile picture to change it. These images are saved in the Firebase Cloud Storage.

## API's and 3rd Party Services
The following list of API's and services are used through MTG Companion.

### Services
* Firebase Realtime Database
* Firebase Cloud Storage
* Firebase Authentication
* Google Authentication

### APIs
* Google Places
* Google Directions

## UX/DX Approach
MTG Companion was developed by closely following the MVVM architecture.

## Git Approach
GitHub was used as the version control system when developing MTG Companion. Each new feature/section was developed on a seperate branch and once it's development was finished or put on pause, the changes were merged into main and new feature was selected for development on a new branch. Once all the functionality was implemeneted, a release was created.  
A second release was planned after the inclusion of Firebase UI, but due to time constraints, Firebase UI was not implemented.

GitHub URL: https://github.com/Jakub-Poczatek/MTG_Companion_V2/tree/main  

## Personal Statement
In conclusion, I believe I was able to fully satisfy the marking scheme for this assignment. However, there is a few features that given time, I would have liked to add. These features include:
* A trading system, where users could upload their cards to a marketplace and buy other user's cards. 
* Integration with the ``Magic The Gathering API``.
* Firebase UI.