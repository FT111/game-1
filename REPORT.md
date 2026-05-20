Module Code: CS1OP

Assignment report Title: Project

Student Number (e.g. 25098635): 34028527

Actual hrs spent for the assignment: 210

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

[//]: # (Challenges and limitations.)
I did find the limitations of LLM models at some points. LLMs are terrible at generating ASCII art, which I ran into while
creating the level's background tilemap. I turned to an LLM for this to supplement my own lacking artistic skills. However,
Claude Sonnet had consistently jagged and mismatched lines between text rows and columns, while Gemini 3.1 Pro, with some very specific prompting, was the only
one I found that had acceptable quality, still with some misalignment that had to be manually fixed. I figured this was 
a result of LLM's training being mostly single line, non ASCII text, resulting in the lacking ability to produce ASCII
art aligned across multiple lines. 


[//]: # (Overall impact on learning and development.)

 
## Analysis of Software Patterns in the Project

[//]: # (How the patterns were used.)

I tried to fit a common software pattern into every aspect of the game where appropriate. The primary, most impactful
ones I applied were in the game engine, where I used the Observer pattern at the core of the event bus. (Weigh against DI with systems, talk about being conservative with subscriptions to save performance)

[//]: # (Benefits of using software patterns.)

[//]: # (Challenges and limitations.)
[//]: # (talk about initial abscene of lifecycle patterns in logic systems)
[//]: # (Overall impact on the project.)

## Ethical and Legal Considerations

[//]: # (Reflect on ethical concerns related to AI tools &#40;e.g.,)

[//]: # (over reliance, biases, academic integrity&#41;.)

[//]: # (Discuss data handling and privacy &#40;e.g., GDPR)

[//]: # (compliance, sensitive data&#41;.)

[//]: # (Broader ethical and legal implications &#40;e.g., misuse,)

[//]: # (accessibility, licensing&#41;.)
 
## Conclusion

[//]: # (Summarize findings, reflect on lessons learned, and provide recommendations.)
 
## References
FirstName, L. (Year) *Title*. Available at: URL (Accessed: Date).

Nicholas, S. (2017) *Easy A\* (star) Pathfinding*. Available at: https://medium.com/@nicholas.w.swift/easy-a-star-pathfinding-7e6689c7f7b2 (Accessed: 14th May 2026).