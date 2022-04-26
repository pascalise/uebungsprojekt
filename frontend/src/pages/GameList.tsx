import { Grid, SelectChangeEvent, TextField } from '@mui/material';
import { useEffect, useState } from 'react';
import GameCard from '../components/GameCard';
import Game from '../model/Game';
import Console from '../model/Console';
import './GameList.sass';
import compareGames from '../util/SortGames';
import ConsoleFilter from '../components/ConsoleFilter';
import GameController from '../middleware/controller/GameController';
import ConsoleController from '../middleware/controller/ConsoleController';

export default function GameList() {
  const [consoles, setConsoles] = useState<Console[]>([])
  const [selectedConsoles, setSelectedConsoles] = useState<string[]>([])
  const [games, setGames] = useState<Game[]>([])
  const [searchedGames, setSearchedGames] = useState<Game[]>([])

  function handleConsoleFilterChange (event: SelectChangeEvent<typeof selectedConsoles>) {
    setSelectedConsoles(event.target.value as string[])
  }

  function handleGameSearchChange (event: React.ChangeEvent<HTMLInputElement>) {
    setSearchedGames(games.filter((game: Game) => game.name.toLowerCase().includes(event.target.value.toLowerCase())))
  }

  useEffect(() => { 
    ConsoleController.getConsoles().then(setConsoles)    
    GameController.getGames().then(games => {
      setGames(games)
      setSearchedGames(games)
    })
  }, [])

  return (
    <div className='game-list'>
      <div className='game-list-settings'>
        <ConsoleFilter consoles={consoles} selectedConsoles={selectedConsoles} handleChange={handleConsoleFilterChange}/>
        <TextField id="outlined-basic" label="Search Game" variant="outlined" margin='dense' onChange={handleGameSearchChange}/>
      </div>
      <div className='game-list-grid'>
        <Grid container spacing={5}>
          {
            searchedGames
            .sort((a: Game, b: Game) => compareGames(a, b))
            .filter((game: Game) => game.consoles.some((c: Console) => selectedConsoles.includes(c.name) || selectedConsoles.length === 0))
            .map((game: Game, idx: number) => {
              return (                
                <Grid item key={idx} xs={4}>
                  <GameCard game={game}/>
                </Grid>
              )
            })
          }
        </Grid>
      </div>
    </div>
  );
}
