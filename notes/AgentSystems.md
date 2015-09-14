- ascape - http://ascape.sourceforge.net/index.html#Introduction ; seems overly intricate, gui'ish and based on a rectangular cell thingy
- jade - http://jade.tilab.com/ ; doesn't seem to offer anything regarding learning
- ecj - http://cs.gmu.edu/~eclab/projects/ecj/
- mason - http://cs.gmu.edu/~eclab/projects/mason/

https://en.wikipedia.org/wiki/Action_selection
https://en.wikipedia.org/wiki/Intelligent_agent
https://en.wikipedia.org/wiki/Markov_decision_process
https://en.wikipedia.org/wiki/Reinforcement_learning
https://en.wikipedia.org/wiki/Q-learning

# Musebot

- http://axon.cs.byu.edu/ICCC2015proceedings/6.2Eigenfeldt.pdf
- https://docs.google.com/document/d/1UtdLYsOErzXKNFxrM7utHeFXgPNcC_w40lTtUxtCYO8/edit
- https://bitbucket.org/obown/musebot-developer-kit

"Each software agent should correspond roughly to a single “instrumental part” in a piece of music, like a bassline or a drum beat." -- ouch!

Makes all sorts of unnecessary assumption such as that each bot is a separate audio process feeding into Jack, etc. The whole conductor architecture doesn't seem to fit here.

"An agent should be a standalone application that can run on Mac OSX 10.10.1 or later." -- LOL, forget it

All it does is sum up audio signals and send OSC messages between agents, there doesn't seem to be anything related to the actual design of the algorithms, their learning, perception, etc.

The paper basically just poses (interesting) questions without having addressed any of them.

The example agents don't learn anything, e.g. https://bitbucket.org/obown/musebot-developer-kit/src/a656a7065bad73387f6ed1c55222eaba0f0a9741/Musebots/Java_Keys_Examplebot/app/src/Musebot%20Example%20Agents/src/org/musicalmetacreation/musebot/example_agents/KeysAgent.java?at=master&fileviewer=file-view-default playing random stuff

# Q Learning

- https://github.com/sandropaganotti/processing.org-q-learning-td-lambda-/tree/master

# Constructing skill trees

https://en.wikipedia.org/wiki/Constructing_skill_trees

(looks complicated)

Seems to be used mainly to train robots

# 

- meta heuristic https://en.wikipedia.org/wiki/Metaheuristic

# Temporal Difference Learning

- https://en.wikipedia.org/wiki/Temporal_difference_learning
- http://webdocs.cs.ualberta.ca/~sutton/book/ebook/node64.html

# Reinforced Learning

http://webdocs.cs.ualberta.ca/~sutton/book

# Links

- https://web.archive.org/web/20130129132727/http://www.elsy.gdan.pl/
- http://www.cse.unsw.edu.au/~cs9417ml/RL1/
- https://webdocs.cs.ualberta.ca/~sutton/software.html
- http://www-anw.cs.umass.edu/rlr/
- http://www.applied-mathematics.net/qlearning/qlearning.html
- http://web.archive.org/web/20090501010345/http://2009.rl-competition.org/domains.php

