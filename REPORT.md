Module Code: CS1OP

Assignment report Title: Project

Student Number: 34028527

Actual hrs spent for the assignment: 211

Which Artificial Intelligence tools used (if applicable): Claude, GitHub Copilot, Gemini (via Github Copilot)

URL of repository: https://csgitlab.reading.ac.uk/qa028527/cs1op-coursework-1

## Introduction

This project is comprised of two separate parts, the game itself and a small engine to run it. The game, at its core, 
is a puzzle game with stealth elements, where the player must infiltrate a corporate headquarters and complete various
objectives while avoiding detection by guards and security systems. Its loosely inspired by the Hitman series, which is 
also a 'stealth' game. In Hitman, players are given the ability to take a freeform approach to solving environmental 
and social puzzles to complete their objectives, but my game obviously doesn't involve killing anyone or any combat at all.
My game leans into the main character being a relatively normal person, rather than a highly trained assassin,
 so the player is made to rely on stealth to complete their objectives, rather than brute force.

I felt creating an 'engine' to run the game was necessary as I wanted to create a game that was more interesting
than a simple text adventure, with 2D text graphics and some complex mechanics. I could have gone about creating the game without an underlying
engine, but I figured it would be much more efficient to create a reusable engine as an abstraction layer, with the core job of 
collecting and applying game logic, and rendering the returned state. 

To **run the game**, open a terminal in the game's root folder, and run `java -jar game.jar` 

 
## Analysis of AI Support in Software Development 
 
[//]: # (How AI tools were used and benefits)
Prior to development, I used an LLM to assist in conceptualising ideas for the game. This was extremely helpful to me 
as I have no game development or writing background, and coming up with a 'fun' idea that any person would realistically
enjoy playing was somewhat difficult. The LLM (Claude Sonnet 4.5 at the time) was instrumental in offloading this creative aspect
 so I could focus on the game's architecture. Some would, rightfully, argue this takes away 'soul' or creativity from the
game, however I'm using it as a practical measure to avoid having the workload of a writer and designer on top of the 
actual, graded aspects of the project.

I could infer some details on how a game engine was structured, i.e having a renderer and some way to store entities, 
but I had little clue on what a realistic engine looked like internally. Claude was helpful in laying out the different
aspects needed at a high level. It also explained to me the concept of Entity-Component-System architecture, which I then
used as a fundamental pattern within the engine. It was vital something like that was made known to me early, as 
shoehorning it in later on would likely have lead to a half-baked, buggy, implementation, and I likely wouldn't have come across
it without an LLM.

A system I created with impactful assistance from an LLM was the InventorySystem. I figured it was a simple system that 
an agentic LLM could easily implement, while I could spend time working on problems worth my time like tuning the guard's 
pathing. I reviewed and tested its code and with some small changes, switching the core data structure to a hashmap, approved it as
a change and commited it to Git. The overall process was much faster than manually writing ~200 lines of fairly simple
code, and had a meaningful positive impact on my productivity.

[//]: # (Challenges and limitations.)
I did find the limitations of LLM models at some points. LLMs are terrible at generating ASCII art, which I ran into while
creating the level's background tilemap. I turned to an LLM for this to supplement my own lacking artistic skills. However,
Claude Sonnet had consistently jagged and mismatched lines between text rows and columns, while Gemini 3.1 Pro, with some very specific prompting, was the only
one I found that had acceptable quality, still with some misalignment that had to be manually fixed. I figured this was 
a result of LLM's training being mostly single line, non ASCII text, resulting in the lacking ability to produce ASCII
art aligned across multiple lines. I eventually gave up and heavily modified a basic Gemini-sourced map to be the tilemap
for the first level.
 
## Analysis of Software Patterns in the Project

[//]: # (How the patterns were used.)

I tried to fit a common software pattern into every aspect of the game where one would be appropriate. The most impactful
ones I applied were in the game engine, where I used the Observer pattern at the core of the event bus.
This fit well, as a game is naturally composed of events which can be passed along to logical systems. Systems  
subscribe to relevant events, as an observer, and process them. Systems can also emit events themselves. 
This had the major effect of entirely decoupling systems from each other, as game logic systems would never call
a method of another, instead they simply place an event on the central queue which would then cascade across subscribers.
This is a significant advantage as it means Systems can work entirely independently of each other, and can be easily
dropped in and out without disrupting the behaviour of other ones. While not relevant to my solo game, decoupling with 
events also helps teams develop systems independently of each other, using the events effectively as shared message contracts.
Another approach I could have taken could have been to rely on Dependency Injection, with shared objects, to pass information
between systems. This would have been feasible, but certainly would have turned into a mess of 'spaghetti code' as I would have
had to manually wire up interactions between a plethora of systems. Although, the event-driven observer pattern option has downsides.
It increased the complexity, even from the very beginning of development, for minimal initial gain. It also needs a lot
of boilerplate code for even simple event effects.

Another core pattern I used was Entity-Component-System design. It gave the engine a consistent, simple framework for 
working with entities, as it makes each entity simply a 'bag of components'. Components are data classes containing related 
information about an entity, giving each entity extremely flexible properties that can be manipulated by changing components.
Systems are classes that query the entities for entities with specific components, allowing components to affect behaviour.
The flexibility was the main advantage of using this architecture. While ECS is also usually highly performant, these
performance advantages of basic data-oriented components are likely lost using a high level language like Java, which
doesn't provide access to efficient data structures like structs. 

A design decision I made was to separate entities (highly dynamic elements, e.g the player) and layers (mostly static tilemap elements, e.g the level).
I often considered reverting this to only have a single 'entities' store, as the two have no structural difference, both 
still being a bag of components. However, I decided separating them wasn't redundant as it improves code intent, making
 code slightly more self-documenting with little effort. This also makes querying elements faster as they are two 
smaller data structures rather than one large one, however this is less important and effectively premature optimisation as the game is far
from the scale where the difference would have a noticeable difference.

A flaw of ECS architecture is that it can be awkward to create some more complex elements, like a user interface. Each UI
element needs sizing, positioning, styling and functionality, which isn't very ergonomic to do with traditional pure ECS.
This is why I added an abstraction over ECS for the UI using the abstract builder pattern to create a more fluent,
declarative experience. The builders were a lot of upfront complexity for an end result which had no additional functionality
outside of DX, however the end result was worth it for the impact of making it easier to focus on the UI itself while making it, rather than its specific implementation.


## Ethical and Legal Considerations

From my viewpoint, using a Large Language Model to generate unchecked (or lazily checked), untested, or little-understood code is
beyond ethical boundaries and has a high risk of including bugs. This is why I used them primarily used them for research and for some code generation. 
if I used code generation, I'd  use it to generate lines of code that I fully understood, and I was sure the LLM would also understand, so I could use 
my time on more complex logic out of scope of an LLM's ability. I believe this increased my productivity and didn't stray into
overreliance as I made sure to hand-write code where most appropriate, such as in the A* algorithm. I also thoroughly 
checked through code I was provided to ensure I understood each line, and had multiple times when I rejected a change simply
due to how many lines it was, as I wouldn't trust my ability to fully check it.


Regarding licencing, the least permissive license among the project's dependencies is Lanterna's GNU Lesser GPL license.
LGPL is still relatively permissive, as I could theoretically make the project closed-source. The most relevant LGPL requirement
is for the Lanterna library code to be accessible and modifiable by end users if the project is distributed. I complied with
this by ensuring Lanterna is dynamically linked rather than compiled with my source code, so it is modifiable even when 
the project is compiled into a JAR file as its classpath can still be inspected and changed. 
I decided on the MIT license as I'm not personally bothered by anyone using the code in the project, although they would
still need to abide by Lanterna's licence. The other dependencies use an Apache 2.0 license, which, like the MIT, have 
few restrictions on derivative work, primarily attribution when distributing, making them less impactful.


The game does not handle any sensitive data, or send any telemetry over the internet. This meant that GDPR or DPA 2018 
compliance was not a major concern throughout the development of the project. The engine could be modified to include 
networked telemetry or process sensitive data by a third party, however the liability would be on them as the software 
is provided 'as is' according to the MIT licence.

Accessibility is a concern in my project as the game relies heavily on visual text elements. There are no integrated 
accessibility settings to assist those with visual impairment. However, every terminal I have checked (Windows 11 Terminal, MacOS Tahoe Terminal, iTerm2) has 
its own font, text size and appearance settings to allow users to adjust the game's rendering to better suit their visual needs. 

## Conclusion

[//]: # (Summarize findings, reflect on lessons learned, and provide recommendations.)
 
In conclusion, the project was mostly a success. The end result is a game, which meets all the specifications given, and 
an underlying lightweight engine to support it. Making the engine went to plan, with the structure of it almost entirely 
matching my planning documentation, however I underestimated the complexity of the game itself and failed to adequately
plan for it, meaning it was a little rushed and not as polished as I aimed for it to be. For the future, this has taught
me to balance my planning evenly across a project, especially on the areas I am less familiar with, and not just focus
on the part I am most interested in making.

## References
FirstName, L. (Year) *Title*. Available at: URL (Accessed: Date).

Javidx9 (2022) *OneLoneCoder_PGE_ShadowCasting2D.cpp*. Available at: https://github.com/OneLoneCoder/Javidx9/blob/master/PixelGameEngine/SmallerProjects/OneLoneCoder_PGE_ShadowCasting2D.cpp (Accessed: 26th March 2026).

Javidx9 (2022) *Line Of Sight or Shadow Casting in 2D*. Available at: https://www.youtube.com/watch?v=fc3nnG2CG8U (Accessed: 26th March 2026).

Free Software Foundation (2007) *GNU Lesser General Public License*. Available at: https://www.gnu.org/licenses/lgpl-3.0.html (Accessed: 21st May 2026).

Open Source Initiative *The MIT License*. Available at: https://opensource.org/license/mit (Accessed: 21st May 2026)

Nicholas, S. (2017) *Easy A\* (star) Pathfinding*. Available at: https://medium.com/@nicholas.w.swift/easy-a-star-pathfinding-7e6689c7f7b2 (Accessed: 14th May 2026).